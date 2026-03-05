package com.resolvehub.common.util;

import java.util.regex.Pattern;

public final class SecretRedactor {
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(api[_-]?key\\s*[=:]\\s*)([A-Za-z0-9_\\-]{8,})", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(token\\s*[=:]\\s*)([A-Za-z0-9_\\-\\.]{8,})", Pattern.CASE_INSENSITIVE);
    private static final Pattern BEARER_PATTERN = Pattern.compile("(bearer\\s+)([A-Za-z0-9_\\-\\.]{8,})", Pattern.CASE_INSENSITIVE);

    private SecretRedactor() {
    }

    public static String redact(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String output = API_KEY_PATTERN.matcher(input).replaceAll("$1***REDACTED***");
        output = TOKEN_PATTERN.matcher(output).replaceAll("$1***REDACTED***");
        return BEARER_PATTERN.matcher(output).replaceAll("$1***REDACTED***");
    }
}
