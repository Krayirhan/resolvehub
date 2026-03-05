package com.resolvehub.playbook.dto;

import java.time.Instant;

public record SolutionResponse(
        Long id,
        Long problemId,
        Long authorId,
        Long rootCauseId,
        Long fixId,
        String summary,
        String stepsMarkdown,
        String risksMarkdown,
        String rollbackMarkdown,
        String verificationMarkdown,
        int voteScore,
        Instant createdAt,
        Instant updatedAt
) {
}
