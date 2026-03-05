package com.resolvehub.playbook.dto;

import com.resolvehub.playbook.domain.PlaybookStatus;

import java.time.Instant;
import java.util.List;

public record PlaybookResponse(
        Long id,
        String title,
        String description,
        Long sourceSolutionId,
        int version,
        PlaybookStatus status,
        List<PlaybookStepResponse> steps,
        Instant createdAt
) {
    public record PlaybookStepResponse(
            int stepNo,
            String title,
            String contentMarkdown
    ) {
    }
}
