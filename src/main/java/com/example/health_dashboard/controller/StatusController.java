package com.example.health_dashboard.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    private final String version;
    private final String environment;

    public StatusController(@Value("${app.version}") String version, @Value("${app.environment}") String environment) {
        this.version = version;
        this.environment = environment;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/version")
    public Map<String, Object> version() {
        return Map.of("version", version);
    }

    @GetMapping("/environment")
    public Map<String, Object> environment() {
        return Map.of("environment", environment);
    }

}
//conflict1
