package com.resolvehub.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class EnvironmentFingerprintUtil {
    private EnvironmentFingerprintUtil() {
    }

    public static String fingerprint(Map<String, String> environmentFacets) {
        Map<String, String> sorted = new TreeMap<>(environmentFacets);
        String canonical = sorted.entrySet().stream()
                .map(e -> e.getKey().trim().toLowerCase() + "=" + e.getValue().trim().toLowerCase())
                .collect(Collectors.joining("|"));
        return sha256(canonical);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }
}
