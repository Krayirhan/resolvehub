package com.resolvehub.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component("openSearch")
public class OpenSearchHealthIndicator implements HealthIndicator {
    private final String endpoint;

    public OpenSearchHealthIndicator(@Value("${resolvehub.search.endpoint:http://localhost:9200}") String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Health health() {
        try {
            RestClient client = RestClient.builder().baseUrl(endpoint).build();
            String response = client.get().uri("/_cluster/health").retrieve().body(String.class);
            return Health.up().withDetail("endpoint", endpoint).withDetail("response", response).build();
        } catch (Exception ex) {
            return Health.unknown().withDetail("endpoint", endpoint).withDetail("reason", ex.getMessage()).build();
        }
    }
}
