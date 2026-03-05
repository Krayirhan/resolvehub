package com.resolvehub.app.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SolutionOutcomeIntegrationTest extends AbstractIntegrationTest {
    @Test
    void shouldCreateClaimAndValidateSolutionAndRecordOutcomesWithFingerprint() {
        String token = registerAndGetAccessToken();
        Long problemId = createProblem(token, Map.of("os", "linux", "java", "21"));
        Integer embedBefore = jdbcTemplate.queryForObject(
                "select count(*) from ai_jobs where type='EMBED_ENTITY' and payload_json like '%\"entityType\":\"SOLUTION\"%'",
                Integer.class
        );

        Integer claimId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of("message", "I solved this in production by tuning pool + DNS."))
                .when()
                .post("/problems/" + problemId + "/claims")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("PENDING"))
                .extract()
                .path("id");

        AuthTokens admin = login("admin@resolvehub.local", "Admin123!");
        given()
                .header("Authorization", "Bearer " + admin.accessToken())
                .contentType("application/json")
                .body(Map.of("status", "APPROVED"))
                .when()
                .patch("/claims/" + claimId)
                .then()
                .statusCode(200)
                .body("status", equalTo("APPROVED"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "summary", "Invalid solution payload",
                        "stepsMarkdown", "1. Step",
                        "risksMarkdown", " ",
                        "rollbackMarkdown", "rollback",
                        "verificationMarkdown", "verify"
                ))
                .when()
                .post("/problems/" + problemId + "/solutions")
                .then()
                .statusCode(400);

        Integer solutionId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "summary", "Increase pool timeout and pin DB host",
                        "stepsMarkdown", "1. Check DB host\n2. Restart pool",
                        "risksMarkdown", "Temporary latency increase",
                        "rollbackMarkdown", "Rollback pool configuration",
                        "verificationMarkdown", "Run smoke tests and check metrics"
                ))
                .when()
                .post("/problems/" + problemId + "/solutions")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");

        String fingerprintWorked = given()
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
                .body("solutionId", equalTo(solutionId))
                .body("outcome", equalTo("WORKED"))
                .extract()
                .path("environmentFingerprint");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "outcome", "FAILED",
                        "environmentFingerprint", fingerprintWorked,
                        "notes", "Failed with same env after a bad rollback"
                ))
                .when()
                .post("/solutions/" + solutionId + "/outcomes")
                .then()
                .statusCode(201)
                .body("solutionId", equalTo(solutionId))
                .body("outcome", equalTo("FAILED"))
                .body("environmentFingerprint", equalTo(fingerprintWorked));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/problems/" + problemId + "/solutions")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(solutionId));

        Integer claimCount = jdbcTemplate.queryForObject(
                "select count(*) from solver_claims where id = ? and problem_id = ?",
                Integer.class,
                claimId.longValue(),
                problemId
        );
        Integer embedJobs = jdbcTemplate.queryForObject(
                "select count(*) from ai_jobs where type='EMBED_ENTITY' and payload_json like '%\"entityType\":\"SOLUTION\"%'",
                Integer.class
        );
        assertEquals(1, claimCount);
        assertEquals(embedBefore + 1, embedJobs);
    }
}
