package com.resolvehub.playbook.dto;

import com.resolvehub.playbook.domain.SolverClaimStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSolverClaimRequest(@NotNull SolverClaimStatus status) {
}
