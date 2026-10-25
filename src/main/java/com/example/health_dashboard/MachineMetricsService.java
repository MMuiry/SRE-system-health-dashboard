package com.example.health_dashboard;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MachineMetricsService {

    private static final Logger log = LoggerFactory.getLogger(MachineMetricsService.class);
    private static final int MAX_ALERTS = 20;

    private final double thresholdPercent;
    private final Map<String, Map<String, Object>> latestByMachine = new ConcurrentHashMap<>();
    private final Deque<String> recentAlerts = new ConcurrentLinkedDeque<>();

    public MachineMetricsService(@Value("${app.alerts.threshold-percent}") double thresholdPercent) {
        this.thresholdPercent = thresholdPercent;
    }

    /** Called by Kafka for every message the agents publish. */
    @KafkaListener(topics = "${app.kafka.metrics-topic}")
    public void onMetrics(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> stats = JsonParserFactory.getJsonParser().parseMap(record.value());
            String machine = record.key() != null ? record.key() : String.valueOf(stats.get("machine"));

            latestByMachine.put(machine, stats);

            checkThreshold(machine, "CPU", stats.get("cpu_percent"), stats.get("timestamp"));
            checkThreshold(machine, "Memory", stats.get("memory_percent"), stats.get("timestamp"));
        } catch (Exception ex) {
            log.warn("Ignoring bad metrics message: {}", ex.getMessage());
        }
    }

    private void checkThreshold(String machine, String metric, Object value, Object timestamp) {
        if (value instanceof Number number && number.doubleValue() > thresholdPercent) {
            recentAlerts.addFirst(machine + " " + metric + " " + number + "% at " + timestamp);
            while (recentAlerts.size() > MAX_ALERTS) {
                recentAlerts.pollLast();
            }
        }
    }

    /** Latest stats for every machine, sorted by machine name. */
    public Map<String, Map<String, Object>> machines() {
        return new TreeMap<>(latestByMachine);
    }

    public List<String> alerts() {
        return List.copyOf(recentAlerts);
    }
}