package com.resolvehub.problemgraph.dto;

import jakarta.validation.constraints.NotNull;

public record CanonicalProblemRequest(@NotNull Long canonicalProblemId) {
}
