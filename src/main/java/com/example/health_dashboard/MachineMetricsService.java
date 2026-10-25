package com.example.health_dashboard;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MachineMetricsService {

    private static final Logger log = LoggerFactory.getLogger(MachineMetricsService.class);

    private final Map<String, Map<String, Object>> latestByMachine = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${app.kafka.metrics-topic}")
    public void onMetrics(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> stats = JsonParserFactory.getJsonParser().parseMap(record.value());
            String machine = record.key() != null ? record.key() : String.valueOf(stats.get("machine"));
            latestByMachine.put(machine, stats);
        } catch (Exception ex) {
            log.warn("Ignoring bad metrics message: {}", ex.getMessage());
        }
    }

    public Map<String, Map<String, Object>> machines() {
        return new TreeMap<>(latestByMachine);
    }
}