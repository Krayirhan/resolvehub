package com.resolvehub.problemgraph.dto;

import com.resolvehub.common.model.ProblemStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProblemResponse(
        Long id,
        Long authorId,
        String title,
        String description,
        String category,
        Map<String, String> environment,
        ProblemStatus status,
        Long canonicalProblemId,
        List<String> tags,
        String triageStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
