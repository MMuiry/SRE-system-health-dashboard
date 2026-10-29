package com.example.health_dashboard.controller;

import com.example.health_dashboard.MachineMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
public class MachinesController {
    private final MachineMetricsService metrics;

    public MachinesController(MachineMetricsService metrics) {
        this.metrics = metrics;
    }

    @GetMapping("/machines")
    public Map<String, Object> machines() {
        Map<String, Map<String, Object>> all = metrics.machines();
        return Map.of(
                "count", all.size(),
                "machines", all);
    }

}
