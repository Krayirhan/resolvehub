package com.resolvehub.playbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSolutionRequest(
        Long rootCauseId,
        Long fixId,
        @NotBlank @Size(min = 10, max = 5000) String summary,
        @NotBlank String stepsMarkdown,
        @NotBlank String risksMarkdown,
        @NotBlank String rollbackMarkdown,
        @NotBlank String verificationMarkdown
) {
}
