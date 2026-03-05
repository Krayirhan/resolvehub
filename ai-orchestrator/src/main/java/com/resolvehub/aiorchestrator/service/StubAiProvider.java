package com.resolvehub.aiorchestrator.service;

import com.resolvehub.aiorchestrator.dto.ProblemTriageResult;
import com.resolvehub.common.util.SecretRedactor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StubAiProvider implements AiProvider {
    private static final Pattern ERROR_PATTERN = Pattern.compile("(Exception|Error|HTTP\\s*\\d{3}|ORA-\\d+|SQLSTATE\\s*\\w+)[^\\n]*", Pattern.CASE_INSENSITIVE);

    @Override
    public ProblemTriageResult triageProblem(String title, String description) {
        String redacted = SecretRedactor.redact(description);
        List<String> tags = inferTags(title + " " + redacted);
        List<String> errors = extractErrors(redacted);
        return new ProblemTriageResult(redacted, tags, errors);
    }

    @Override
    public List<Double> embed(String content) {
        // Deterministic lightweight embedding stub for local/dev flow.
        int dim = 32;
        double[] vector = new double[dim];
        String text = content == null ? "" : content.toLowerCase(Locale.ROOT);
        for (int i = 0; i < text.length(); i++) {
            vector[i % dim] += ((int) text.charAt(i)) / 255.0;
        }
        List<Double> result = new ArrayList<>(dim);
        for (double value : vector) {
            result.add(value);
        }
        return result;
    }

    @Override
    public String providerName() {
        return "stub";
    }

    private List<String> inferTags(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> tags = new ArrayList<>();
        if (lower.contains("postgres") || lower.contains("sql")) tags.add("database");
        if (lower.contains("redis")) tags.add("redis");
        if (lower.contains("docker") || lower.contains("container")) tags.add("containers");
        if (lower.contains("timeout")) tags.add("timeout");
        if (lower.contains("memory") || lower.contains("oom")) tags.add("memory");
        if (tags.isEmpty()) tags.add("triaged");
        return tags;
    }

    private List<String> extractErrors(String text) {
        Matcher matcher = ERROR_PATTERN.matcher(text);
        List<String> results = new ArrayList<>();
        while (matcher.find() && results.size() < 5) {
            results.add(matcher.group().trim());
        }
        return results;
    }
}
