package com.resolvehub.app.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class RecommendationRankingIntegrationTest extends AbstractIntegrationTest {
    @Test
    void shouldReorderRecommendationsAfterContextOutcomes() {
        String tokenA = registerAndGetAccessToken();
        String tokenB = registerAndGetAccessToken();

        Long problemId = createProblem(tokenA, Map.of("os", "linux", "java", "21", "db", "postgres"));
        Long solutionA = createSolution(tokenA, problemId, "Fix by increasing connection timeout and restarting pool");
        Long solutionB = createSolution(tokenB, problemId, "Fix by disabling retries and using fallback cache");

        // Seed initial outcomes so B starts weaker and A becomes dominant for matching context.
        given()
                .header("Authorization", "Bearer " + tokenA)
                .contentType("application/json")
                .body(Map.of(
                        "outcome", "WORKED",
                        "environment", Map.of("os", "linux", "java", "21", "db", "postgres"),
                        "notes", "Worked in prod"
                ))
                .post("/solutions/" + solutionA + "/outcomes")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + tokenB)
                .contentType("application/json")
                .body(Map.of(
                        "outcome", "FAILED",
                        "environment", Map.of("os", "linux", "java", "21", "db", "postgres"),
                        "notes", "No improvement"
                ))
                .post("/solutions/" + solutionB + "/outcomes")
                .then()
                .statusCode(201);

        Response response = given()
                .header("Authorization", "Bearer " + tokenA)
                .when()
                .get("/problems/" + problemId + "/recommended")
                .then()
                .statusCode(200)
                .extract()
                .response();

        Integer firstSolutionId = response.path("topSolutions[0].solutionId");
        org.junit.jupiter.api.Assertions.assertEquals(solutionA.intValue(), firstSolutionId);

        given()
                .header("Authorization", "Bearer " + tokenA)
                .when()
                .get("/problems/" + problemId + "/recommended")
                .then()
                .statusCode(200)
                .body("topSolutions[0].solutionId", equalTo(solutionA.intValue()));
    }
}
