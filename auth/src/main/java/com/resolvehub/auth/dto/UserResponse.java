package com.resolvehub.auth.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String username,
        Set<String> roles
) {
}
