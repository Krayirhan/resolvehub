package com.resolvehub.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component("minio")
public class MinioHealthIndicator implements HealthIndicator {
    private final String endpoint;

    public MinioHealthIndicator(@Value("${resolvehub.storage.endpoint:http://localhost:9000}") String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Health health() {
        try {
            RestClient client = RestClient.builder().baseUrl(endpoint).build();
            client.get().uri("/minio/health/live").retrieve().toBodilessEntity();
            return Health.up().withDetail("endpoint", endpoint).build();
        } catch (Exception ex) {
            return Health.unknown().withDetail("endpoint", endpoint).withDetail("reason", ex.getMessage()).build();
        }
    }
}
