package com.resolvehub.playbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PromotePlaybookRequest(
        @NotBlank @Size(min = 5, max = 300) String title,
        @NotBlank @Size(min = 10, max = 3000) String description
) {
}
