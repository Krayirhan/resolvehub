package com.resolvehub.problemgraph.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddTagRequest(@NotBlank @Size(max = 100) String tag) {
}
