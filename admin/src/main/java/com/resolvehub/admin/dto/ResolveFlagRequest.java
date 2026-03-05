package com.resolvehub.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveFlagRequest(@NotBlank String status) {
}
