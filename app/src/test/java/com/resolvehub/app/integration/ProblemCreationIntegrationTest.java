package com.resolvehub.app.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class ProblemCreationIntegrationTest extends AbstractIntegrationTest {
    @Test
    void shouldCreateProblemAndReturnPendingTriageStatus() {
        String token = registerAndGetAccessToken();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "title", "Dockerized app cannot reach postgres",
                        "description", "The service started failing after migration with SQL timeout and repeated connection reset messages",
                        "category", "database",
                        "environment", Map.of("os", "linux", "java", "21")
                ))
                .when()
                .post("/problems")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("triageStatus", equalTo("PENDING"))
                .body("status", equalTo("OPEN"));
    }
}
