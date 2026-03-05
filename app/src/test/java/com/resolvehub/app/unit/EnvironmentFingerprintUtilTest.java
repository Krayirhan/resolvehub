package com.resolvehub.app.unit;

import com.resolvehub.common.util.EnvironmentFingerprintUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EnvironmentFingerprintUtilTest {
    @Test
    void shouldGenerateSameFingerprintForEquivalentMaps() {
        String first = EnvironmentFingerprintUtil.fingerprint(Map.of("os", "Linux", "java", "21"));
        String second = EnvironmentFingerprintUtil.fingerprint(Map.of("java", "21", "os", "linux"));
        assertEquals(first, second);
    }

    @Test
    void shouldGenerateDifferentFingerprintForDifferentEnvironment() {
        String first = EnvironmentFingerprintUtil.fingerprint(Map.of("os", "linux", "java", "21"));
        String second = EnvironmentFingerprintUtil.fingerprint(Map.of("os", "linux", "java", "17"));
        assertNotEquals(first, second);
    }
}
