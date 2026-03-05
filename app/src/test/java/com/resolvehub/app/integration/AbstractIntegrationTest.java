package com.resolvehub.app.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg15")
            .withDatabaseName("resolvehub")
            .withUsername("resolvehub")
            .withPassword("resolvehub");

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("resolvehub.ai.enabled", () -> false);
        registry.add("resolvehub.storage.endpoint", () -> "");
        registry.add("resolvehub.search.endpoint", () -> "http://localhost:9200");
    }

    @BeforeEach
    void setupRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    protected String registerAndGetAccessToken() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Response response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "user-" + suffix + "@test.local",
                        "username", "user_" + suffix,
                        "password", "Password123!"
                ))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .extract()
                .response();
        return response.path("accessToken");
    }

    protected Long createProblem(String token, Map<String, String> environment) {
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "title", "Postgres connection timeout in production",
                        "description", "After deploying today, every service call started failing with TimeoutException while connecting to postgres",
                        "category", "database",
                        "environment", environment
                ))
                .when()
                .post("/problems")
                .then()
                .statusCode(201)
                .extract()
                .response();
        return response.path("id");
    }

    protected Long createSolution(String token, Long problemId, String summary) {
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "summary", summary,
                        "stepsMarkdown", "1. Check DB host\n2. Restart pool",
                        "risksMarkdown", "Temporary latency increase",
                        "rollbackMarkdown", "Rollback pool configuration",
                        "verificationMarkdown", "Run smoke tests and check metrics"
                ))
                .when()
                .post("/problems/" + problemId + "/solutions")
                .then()
                .statusCode(201)
                .extract()
                .response();
        return response.path("id");
    }
}
