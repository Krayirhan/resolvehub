package com.resolvehub.app.integration;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

class AuthFlowIntegrationTest extends AbstractIntegrationTest {
    @Test
    void shouldSupportRegisterLoginRefreshLogoutWithBcryptHash() {
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String email = "auth-user-" + suffix + "@test.local";
        String username = "auth_user_" + suffix;

        AuthTokens registered = registerUser(email, username, "Password123!");

        assertNotNull(registered.accessToken());
        assertNotNull(registered.refreshToken());
        assertNotNull(registered.userId());

        String passwordHash = jdbcTemplate.queryForObject(
                "select password_hash from users where id = ?",
                String.class,
                registered.userId()
        );
        assertNotNull(passwordHash);
        assertNotEquals("Password123!", passwordHash);
        assertTrue(passwordHash.startsWith("$2"));

        AuthTokens login = login(email, "Password123!");
        assertNotNull(login.accessToken());
        assertNotNull(login.refreshToken());
        assertEquals(registered.userId(), login.userId());

        AuthTokens refreshed = refresh(login.refreshToken());
        assertNotNull(refreshed.accessToken());
        assertNotNull(refreshed.refreshToken());
        assertEquals(login.userId(), refreshed.userId());
        assertNotEquals(login.accessToken(), refreshed.accessToken());

        logout(login.refreshToken());

        given()
                .contentType("application/json")
                .body(java.util.Map.of("refreshToken", login.refreshToken()))
                .when()
                .post("/auth/refresh")
                .then()
                .statusCode(401)
                .body("code", equalTo("UNAUTHORIZED"))
                .body("message", notNullValue());
    }
}
