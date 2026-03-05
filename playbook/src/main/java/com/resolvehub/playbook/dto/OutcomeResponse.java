package com.resolvehub.playbook.dto;

import com.resolvehub.common.model.OutcomeType;

import java.time.Instant;

public record OutcomeResponse(
        Long id,
        Long solutionId,
        Long userId,
        String environmentFingerprint,
        OutcomeType outcome,
        String notes,
        Instant createdAt
) {
}
