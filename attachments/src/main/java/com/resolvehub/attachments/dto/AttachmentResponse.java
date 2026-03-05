package com.resolvehub.attachments.dto;

import java.time.Instant;

public record AttachmentResponse(
        Long id,
        String ownerType,
        Long ownerId,
        String objectKey,
        String contentType,
        long size,
        Instant createdAt
) {
}
