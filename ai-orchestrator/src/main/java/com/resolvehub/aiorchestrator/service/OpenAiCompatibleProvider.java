package com.resolvehub.aiorchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.aiorchestrator.config.AiProviderProperties;
import com.resolvehub.aiorchestrator.dto.ProblemTriageResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleProvider implements AiProvider {
    private final AiProviderProperties properties;
    private final ObjectMapper objectMapper;
    private final StubAiProvider fallback;

    public OpenAiCompatibleProvider(AiProviderProperties properties, ObjectMapper objectMapper, StubAiProvider fallback) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.fallback = fallback;
    }

    @Override
    public ProblemTriageResult triageProblem(String title, String description) {
        if (!isConfigured()) {
            return fallback.triageProblem(title, description);
        }
        try {
            RestClient client = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a problem triage assistant. Return JSON with fields: redactedDescription,suggestedTags,extractedErrors"),
                            Map.of("role", "user", "content", "Title: " + title + "\nDescription: " + description)
                    ),
                    "temperature", 0.1
            );
            String response = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode parsed = objectMapper.readTree(content);
            return new ProblemTriageResult(
                    parsed.path("redactedDescription").asText(description),
                    readStringArray(parsed.path("suggestedTags")),
                    readStringArray(parsed.path("extractedErrors"))
            );
        } catch (Exception ex) {
            return fallback.triageProblem(title, description);
        }
    }

    @Override
    public List<Double> embed(String content) {
        if (!isConfigured()) {
            return fallback.embed(content);
        }
        try {
            RestClient client = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
            Map<String, Object> body = Map.of(
                    "model", properties.getEmbeddingModel(),
                    "input", content
            );
            String response = client.post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode embedding = root.path("data").path(0).path("embedding");
            List<Double> result = new ArrayList<>();
            embedding.forEach(node -> result.add(node.asDouble()));
            return result.isEmpty() ? fallback.embed(content) : result;
        } catch (Exception ex) {
            return fallback.embed(content);
        }
    }

    @Override
    public String providerName() {
        return "openai-compatible";
    }

    private boolean isConfigured() {
        return properties.isEnabled()
                && properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank()
                && properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    private List<String> readStringArray(JsonNode node) {
        List<String> values = new ArrayList<>();
        node.forEach(v -> values.add(v.asText()));
        return values;
    }
}
