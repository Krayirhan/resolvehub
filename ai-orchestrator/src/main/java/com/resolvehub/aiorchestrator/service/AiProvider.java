package com.resolvehub.aiorchestrator.service;

import com.resolvehub.aiorchestrator.dto.ProblemTriageResult;

import java.util.List;

public interface AiProvider {
    ProblemTriageResult triageProblem(String title, String description);

    List<Double> embed(String content);

    String providerName();
}
