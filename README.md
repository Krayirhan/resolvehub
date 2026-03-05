# ResolveHub

ResolveHub is a production-minded, problem-resolution platform backend where the core object is a **Problem** and outcomes are tracked as **Solutions**, **Playbooks**, and evidence-backed recommendations.

## System Overview

```text
                 +-------------------------+
                 |     Spring Boot App     |
                 |      (app module)       |
                 +-----------+-------------+
                             |
      +----------------------+-----------------------+
      |                      |                       |
+-----v-----+         +------v------+        +------v------+
|   auth    |         | problem-graph|       |   playbook  |
| JWT/RBAC  |         | problems/tags|       | solutions   |
+-----+-----+         +------+-------+       | outcomes    |
      |                      |               | ranking     |
      |              +-------v-------+       +------+------+
      |              | ai-orchestrator|             |
      |              | triage/jobs/RAG|             |
      |              +-------+--------+             |
      |                      |                      |
      +-----------+----------+----------+-----------+
                  |                     |
            +-----v----+         +------v------+
            | Postgres |         |    Redis    |
            | pgvector |         | rate limit  |
            +----------+         +-------------+
                  |
           +------v------+     +---------------+
           | OpenSearch  |     |     MinIO     |
           | keyword idx |     | attachments   |
           +-------------+     +---------------+
```

## Modules

- `common`: shared errors, security primitives, util classes.
- `auth`: register/login/refresh/logout, JWT, RBAC, rate limiting.
- `problem-graph`: problems, tags, canonical linking, graph entities.
- `playbook`: claims, solutions, outcomes, playbooks, recommendation ranking.
- `search`: hybrid search endpoint + reindex trigger.
- `ai-orchestrator`: quality gate, provider abstraction, AI jobs state machine.
- `attachments`: MinIO-compatible upload + attachment metadata.
- `admin`: moderation actions, flags, audit logs.
- `app`: boot module, Flyway, actuator, OpenAPI, seed data.

## Local Run

### 1) Build

```bash
mvn -q -DskipTests package
```

### 2) Start infra

```bash
docker compose up -d
```

### 3) Run application

```bash
mvn -q -pl app spring-boot:run
```

Default app URL: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui`

Actuator health: `http://localhost:8080/actuator/health`

Prometheus metrics: `http://localhost:8080/actuator/prometheus`

## AI Configuration

Set environment variables (OpenAI-compatible):

- `AI_ENABLED` (`true|false`)
- `AI_BASE_URL`
- `AI_API_KEY`
- `AI_MODEL`
- `EMBEDDING_MODEL`
- `EMBEDDING_DIM`

Also available:

- `JWT_SECRET`, `JWT_ISSUER`
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`
- `OPENSEARCH_URL`
- `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`

## Vertical Slice cURL

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@test.local","username":"user1","password":"Password123!"}'
```

```bash
# Create problem
curl -X POST http://localhost:8080/api/v1/problems \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Postgres timeout","description":"TimeoutException after deploy ...","category":"database","environment":{"os":"linux","java":"21","db":"postgres"}}'
```

```bash
# Create solution
curl -X POST http://localhost:8080/api/v1/problems/1/solutions \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"summary":"Increase pool timeout","stepsMarkdown":"1. Update pool","risksMarkdown":"Latency spike","rollbackMarkdown":"Revert pool config","verificationMarkdown":"Smoke test"}'
```

```bash
# Record outcome
curl -X POST http://localhost:8080/api/v1/solutions/1/outcomes \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"outcome":"WORKED","environment":{"os":"linux","java":"21","db":"postgres"},"notes":"Recovered"}'
```

```bash
# Get recommendations
curl -X GET http://localhost:8080/api/v1/problems/1/recommended \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## Testing

- Unit tests include ranking formula and environment fingerprinting.
- Integration tests use Testcontainers (`pgvector/pgvector:pg15`) and cover:
  - problem creation
  - solution + outcome flow
  - recommendation ordering behavior

Run:

```bash
mvn test
```

If Docker is unavailable, Testcontainers integration tests are skipped (`disabledWithoutDocker=true`).

## Phase 2 Roadmap

- Collaborative live problem sessions (SSE/WebSocket).
- Bounty and incentive schema activation.
- Advanced moderation automation + policy workflows.
- Multi-agent AI pipeline (triage, verifier, citation auditor).
- Graph visualization API/UI support for knowledge graph navigation.
