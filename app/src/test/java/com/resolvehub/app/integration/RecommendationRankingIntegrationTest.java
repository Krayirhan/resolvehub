package com.resolvehub.app.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationRankingIntegrationTest extends AbstractIntegrationTest {
    @Test
    void shouldRankByContextSuccessRecencyAndAuthorExpertiseWithBreakdown() {
        AuthTokens expert = registerUser("expert@test.local", "expert_user", "Password123!");
        AuthTokens novice = registerUser("novice@test.local", "novice_user", "Password123!");

        Long mainProblemId = createProblem(expert.accessToken(), Map.of("os", "linux", "java", "21", "db", "postgres"));
        Long expertSolutionId = createSolution(expert.accessToken(), mainProblemId, "Increase timeout and tune Hikari pool");
        Long noviceSolutionId = createSolution(novice.accessToken(), mainProblemId, "Disable retries and rely on fallback cache");

        // Make expert solution older so recency is lower, then let context success + expertise dominate.
        jdbcTemplate.update("update solutions set created_at = now() - interval '120 days', updated_at = now() - interval '120 days' where id = ?", expertSolutionId);

        // Add another successful historical solution for expert to boost author expertise.
        Long secondaryProblemId = createProblem(expert.accessToken(), Map.of("os", "linux", "java", "21", "db", "postgres"));
        Long secondaryExpertSolution = createSolution(expert.accessToken(), secondaryProblemId, "Use deterministic DNS and increase keepalive");

        given()
                .header("Authorization", "Bearer " + expert.accessToken())
                .contentType("application/json")
                .body(Map.of(
                        "outcome", "WORKED",
                        "environment", Map.of("os", "linux", "java", "21", "db", "postgres"),
                        "notes", "Worked in prod"
                ))
                .post("/solutions/" + expertSolutionId + "/outcomes")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + expert.accessToken())
                .contentType("application/json")
                .body(Map.of(
                        "outcome", "WORKED",
                        "environment", Map.of("os", "linux", "java", "21", "db", "postgres"),
                        "notes", "Second win for expertise"
                ))
                .post("/solutions/" + secondaryExpertSolution + "/outcomes")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + novice.accessToken())
                .contentType("application/json")
                .body(Map.of(
                        "outcome", "FAILED",
                        "environment", Map.of("os", "linux", "java", "21", "db", "postgres"),
                        "notes", "No improvement"
                ))
                .post("/solutions/" + noviceSolutionId + "/outcomes")
                .then()
                .statusCode(201);

        Response response = given()
                .header("Authorization", "Bearer " + expert.accessToken())
                .when()
                .get("/problems/" + mainProblemId + "/recommended")
                .then()
                .statusCode(200)
                .body("topSolutions[0].ranking.successRateContext", greaterThan(0.0f))
                .body("topSolutions[0].ranking.authorExpertise", greaterThan(0.0f))
                .extract()
                .response();

        Integer topSolutionId = response.path("topSolutions[0].solutionId");
        Double topSuccess = response.path("topSolutions[0].ranking.successRateContext");
        Double topExpertise = response.path("topSolutions[0].ranking.authorExpertise");
        Double topRecency = response.path("topSolutions[0].ranking.recencyDecay");
        String topExplanation = response.path("topSolutions[0].ranking.explanation");

        Integer secondSolutionId = response.path("topSolutions[1].solutionId");
        Double secondRecency = response.path("topSolutions[1].ranking.recencyDecay");
        Double secondSuccess = response.path("topSolutions[1].ranking.successRateContext");

        assertEquals(expertSolutionId.intValue(), topSolutionId);
        assertEquals(noviceSolutionId.intValue(), secondSolutionId);
        assertTrue(topSuccess > secondSuccess);
        assertTrue(topExpertise > 0.0);
        assertTrue(topRecency < secondRecency);
        assertTrue(topExplanation.contains("successRateContext"));

        given()
                .header("Authorization", "Bearer " + expert.accessToken())
                .when()
                .get("/problems/" + mainProblemId + "/recommended")
                .then()
                .statusCode(200)
                .body("topSolutions[0].solutionId", equalTo(expertSolutionId.intValue()))
                .body("topSolutions[0].ranking.recencyDecay", lessThan(1.0f));
    }
}
