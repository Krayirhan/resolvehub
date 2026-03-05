package com.resolvehub.playbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSolverClaimRequest(@NotBlank @Size(max = 2000) String message) {
}
