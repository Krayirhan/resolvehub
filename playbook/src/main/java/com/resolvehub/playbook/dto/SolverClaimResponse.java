package com.resolvehub.playbook.dto;

import com.resolvehub.playbook.domain.SolverClaimStatus;

import java.time.Instant;

public record SolverClaimResponse(
        Long id,
        Long problemId,
        Long userId,
        String message,
        SolverClaimStatus status,
        Instant createdAt
) {
}
