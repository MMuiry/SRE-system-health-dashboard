package com.example.health_dashboard;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"KAFKA_ENABLED=true"})
@EnabledIfEnvironmentVariable(named = "KAFKA_BOOTSTRAP_SERVERS", matches = ".+")
class KafkaRoundTripTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    @Test
    void healthCallRoundTripsThroughKafka() throws Exception {
        String before = get("/events");
        get("/health");

        for (int i = 0; i < 20; i++) {
            String after = get("/events");
            if (!after.equals(before) && after.contains("health-check UP")) {
                return;
            }
            Thread.sleep(1000);
        }
        fail("No new health event arrived from Kafka within 20 s");
    }
}