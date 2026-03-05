package com.resolvehub.problemgraph.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateProblemRequest(
        @NotBlank @Size(min = 5, max = 300) String title,
        @NotBlank @Size(min = 20, max = 15000) String description,
        @NotBlank @Size(max = 100) String category,
        @NotEmpty Map<String, String> environment
) {
}
