package com.example.health_dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"APP_ENV=junit", "APP_VERSION=9.9.9"})

class StatusControllerTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * True if the JSON body contains "key":"value".
     */
    private static boolean hasField(String body, String key, String value) {
        return body.contains("\"" + key + "\":\"" + value + "\"");
    }

    @Test
    void healthReturnsUp() throws Exception {
        HttpResponse<String> response = get("/health");
        assertEquals(200, response.statusCode(),
                "/health should return HTTP 200");
        assertTrue(hasField(response.body(), "status", "UP"),
                () -> "/health should report status UP, but returned: " + response.body());
    }

    @Test
    void versionComesFromAppVersion() throws Exception {
        HttpResponse<String> response = get("/version");
        assertEquals(200, response.statusCode(),
                "/version should return HTTP 200");
        assertTrue(hasField(response.body(), "version", "9.9.9"),
                () -> "/version should come from APP_VERSION (9.9.9), but returned: " + response.body());
    }

    @Test
    void environmentComesFromAppEnv() throws Exception {
        HttpResponse<String> response = get("/environment");
        assertEquals(200, response.statusCode(),
                "/environment should return HTTP 200");
        assertTrue(hasField(response.body(), "environment", "junit"),
                () -> "/environment should come from APP_ENV (junit), but returned: " + response.body());
    }

}