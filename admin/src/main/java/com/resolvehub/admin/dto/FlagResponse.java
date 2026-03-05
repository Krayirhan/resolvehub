package com.resolvehub.admin.dto;

import java.time.Instant;

public record FlagResponse(
        Long id,
        String entityType,
        Long entityId,
        String reason,
        Long createdBy,
        String status,
        Instant createdAt
) {
}
