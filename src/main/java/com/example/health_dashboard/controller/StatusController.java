package com.example.health_dashboard.controller;
import java.time.Instant;
import java.util.Map;
import com.example.health_dashboard.HealthEventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    private final String version;
    private final String environment;
    private final HealthEventService healthEvents;

    public StatusController(@Value("${app.version}") String version,
                            @Value("${app.environment}") String environment,
                            HealthEventService healthEvents) {
        this.version = version;
        this.environment = environment;
        this.healthEvents = healthEvents;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        healthEvents.publish("UP");
        return Map.of(
                "status", "UP");
    }

    @GetMapping("/version")
    public Map<String, Object> version() {
        return Map.of("version", version);
    }

    @GetMapping("/environment")
    public Map<String, Object> environment() {
        return Map.of("environment", environment);
    }

    @GetMapping("/events")
    public Map<String, Object> events() {
        return Map.of(
                "kafkaEnabled", healthEvents.isEnabled(),
                "events", healthEvents.recentEvents());
    }


}