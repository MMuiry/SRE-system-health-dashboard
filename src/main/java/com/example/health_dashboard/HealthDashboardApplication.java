package com.example.health_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HealthDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthDashboardApplication.class, args);
	}
}