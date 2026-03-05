package com.resolvehub.aiorchestrator.service;

import com.resolvehub.aiorchestrator.dto.ProblemTriageResult;
import com.resolvehub.common.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QualityGateService {
    private final AiProvider aiProvider;

    public QualityGateService(AiProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    public ProblemTriageResult triageProblem(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Problem title is required");
        }
        if (description == null || description.isBlank()) {
            throw new BadRequestException("Problem description is required");
        }
        return aiProvider.triageProblem(title, description);
    }

    public Set<String> inferTagsFromEnvironment(Map<String, String> environment) {
        Set<String> tags = new HashSet<>();
        environment.forEach((key, value) -> {
            if (key != null && !key.isBlank()) tags.add(key.trim().toLowerCase());
            if (value != null && !value.isBlank()) tags.add(value.trim().toLowerCase().replace(" ", "-"));
        });
        return tags;
    }

    public void validateSolutionStructure(String steps, String risks, String verification, String rollback) {
        if (isBlank(steps) || isBlank(risks) || isBlank(verification) || isBlank(rollback)) {
            throw new BadRequestException("Solution must include steps, risks, verification and rollback sections");
        }
    }

    public List<Double> embeddingFor(String content) {
        return aiProvider.embed(content);
    }

    public String providerName() {
        return aiProvider.providerName();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
