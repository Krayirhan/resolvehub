package com.resolvehub.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ModerationActionRequest(
        @NotBlank String actionType,
        @NotBlank String entityType,
        @NotNull Long entityId,
        Map<String, Object> details
) {
}
