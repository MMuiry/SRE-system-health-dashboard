package com.example.health_dashboard;

import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class HealthEventService {

    private static final Logger log = LoggerFactory.getLogger(HealthEventService.class);
    private static final int MAX_EVENTS = 20;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final boolean enabled;
    private final String topic;
    private final Deque<String> recentEvents = new ConcurrentLinkedDeque<>();

    public HealthEventService(KafkaTemplate<String, String> kafkaTemplate,
                              @Value("${app.kafka.enabled}") boolean enabled,
                              @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.enabled = enabled;
        this.topic = topic;
    }

    /** Sender: runs in the background (@Async) so /health never waits for Kafka. */
    @Async
    public void publish(String status) {
        if (!enabled) {
            return;
        }
        String event = "health-check " + status + " " + Instant.now();
        try {
            kafkaTemplate.send(topic, event).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("Health event not delivered: {}", ex.getMessage());
                } else {
                    log.info("Health event delivered to {} partition {} offset {}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception ex) {
            log.warn("Kafka unavailable, health event not sent: {}", ex.getMessage());
        }
    }

    /** Reader: Kafka calls this for each message; keeps the newest 20. */
    @KafkaListener(topics = "${app.kafka.topic}")
    public void onEvent(String event) {
        recentEvents.addFirst(event);
        while (recentEvents.size() > MAX_EVENTS) {
            recentEvents.pollLast();
        }
    }

    public List<String> recentEvents() {
        return List.copyOf(recentEvents);
    }

    public boolean isEnabled() {
        return enabled;
    }
}