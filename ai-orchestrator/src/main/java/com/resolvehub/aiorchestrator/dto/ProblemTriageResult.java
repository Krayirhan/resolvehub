package com.resolvehub.aiorchestrator.dto;

import java.util.List;

public record ProblemTriageResult(
        String redactedDescription,
        List<String> suggestedTags,
        List<String> extractedErrors
) {
}
