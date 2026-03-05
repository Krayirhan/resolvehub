package com.resolvehub.app.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class SolutionOutcomeIntegrationTest extends AbstractIntegrationTest {
    @Test
    void shouldCreateSolutionAndRecordOutcome() {
        String token = registerAndGetAccessToken();
        Long problemId = createProblem(token, Map.of("os", "linux", "java", "21"));
        Long solutionId = createSolution(token, problemId, "Increase pool timeout and pin DB host");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "outcome", "WORKED",
                        "environment", Map.of("os", "linux", "java", "21"),
                        "notes", "Resolved after tuning HikariCP"
                ))
                .when()
                .post("/solutions/" + solutionId + "/outcomes")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("solutionId", equalTo(solutionId.intValue()))
                .body("outcome", equalTo("WORKED"));
    }
}
