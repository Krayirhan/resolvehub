package com.resolvehub.playbook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record VoteSolutionRequest(
        @Min(-1) @Max(1) int vote
) {
}
