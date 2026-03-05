package com.resolvehub.playbook.dto;

import com.resolvehub.common.model.OutcomeType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record RecordOutcomeRequest(
        @NotNull OutcomeType outcome,
        Map<String, String> environment,
        String environmentFingerprint,
        String notes
) {
}
