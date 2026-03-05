package com.resolvehub.app.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemCreationIntegrationTest extends AbstractIntegrationTest {
    @Test
    void shouldCreateAndFetchAndListProblemsAndEnqueueAiJobs() {
        AuthTokens user = registerUser();
        String token = user.accessToken();
        Integer triageBefore = jdbcTemplate.queryForObject(
                "select count(*) from ai_jobs where type='TRIAGE_PROBLEM'",
                Integer.class
        );
        Integer embedBefore = jdbcTemplate.queryForObject(
                "select count(*) from ai_jobs where type='EMBED_ENTITY' and payload_json like '%\"entityType\":\"PROBLEM\"%'",
                Integer.class
        );

        Integer createdId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "title", "Dockerized app cannot reach postgres",
                        "description", "The service started failing after migration with SQL timeout and repeated connection reset messages",
                        "category", "database",
                        "environment", Map.of("os", "linux", "java", "21", "db", "postgres")
                ))
                .when()
                .post("/problems")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("triageStatus", equalTo("PENDING"))
                .body("status", equalTo("OPEN"))
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/problems/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("category", equalTo("database"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/problems?query=postgres&status=OPEN")
                .then()
                .statusCode(200)
                .body("items.id", hasItem(createdId));

        Integer triageJobs = jdbcTemplate.queryForObject(
                "select count(*) from ai_jobs where type='TRIAGE_PROBLEM'",
                Integer.class
        );
        Integer embedJobs = jdbcTemplate.queryForObject(
                "select count(*) from ai_jobs where type='EMBED_ENTITY' and payload_json like '%\"entityType\":\"PROBLEM\"%'",
                Integer.class
        );
        assertEquals(triageBefore + 1, triageJobs);
        assertEquals(embedBefore + 1, embedJobs);

        String redactedDescription = jdbcTemplate.queryForObject(
                "select description from problems where id = ?",
                String.class,
                createdId.longValue()
        );
        assertTrue(redactedDescription.contains("timeout"));
    }
}
