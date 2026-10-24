# CareSphere AI — Healthcare Document Intelligence Platform

A secure, AI-powered healthcare document intelligence platform. Patients, clinicians,
and administrators manage medical records, upload documents, and ask questions answered
by Retrieval-Augmented Generation (RAG) grounded in their own documents — with validated
source citations.

> **Educational portfolio project.** This is not a medical diagnostic system and must not
> be used for clinical decision-making. It uses synthetic patient data only.

## Technology stack

| Area | Choice |
| --- | --- |
| Language / framework | Java 17, Spring Boot 3.4.x |
| Security | Spring Security + JWT |
| Persistence | Spring Data JPA, PostgreSQL 16 |
| Migrations | Flyway |
| AI / RAG | Spring AI 1.0.x, pgvector |
| Frontend | React + JavaScript |
| Cloud | AWS (Secrets Manager, deployment) |
| Build / test | Maven, JUnit 5, Testcontainers |

## Architecture

A **modular monolith**: a single deployable Spring Boot application organized into clearly
separated package modules that can be extracted into services later if needed.

```
com.caresphere
├── config       # cross-cutting configuration (JPA auditing, OpenAPI, security)
├── common       # shared kernel: exception handling, audit, common DTOs
├── auth         # authentication & authorization module
├── patient      # patient registration, profiles, medical records
├── document     # secure uploads, PDF text extraction
└── ai           # RAG retrieval, grounded answers, citations
```

Each module follows the same layout: `controller` → `service` → `repository` → `domain`,
with `dto` for request/response contracts. Dependencies point inward toward the domain.

## Roadmap (incremental milestones)

1. **Foundation** — Spring Boot skeleton, PostgreSQL + pgvector, Flyway, OpenAPI, exception handling. ✅
2. **Authentication & authorization** — Spring Security, JWT, BCrypt, role-based access. ✅ *(current)*
3. Patient APIs — registration, profiles, medical records (validation, pagination).
4. Document ingestion — secure uploads, PDF text extraction.
5. RAG search — Spring AI, pgvector, grounded answers with citations.
6. Hardening — audit logging, tenant isolation, input validation review.
7. React frontend.
8. Automated tests (unit + integration).
9. AWS deployment with GitHub Actions.

## Authentication

- `POST /api/auth/register` — register a clinician account (201), returns a JWT.
- `POST /api/auth/login` — authenticate (200), returns a JWT.
- Send the token on protected endpoints: `Authorization: Bearer <token>`.
- Roles: `CLINICIAN` (default for self-registration) and `ADMIN`. `/api/admin/**` requires `ADMIN`.
- A default admin is bootstrapped on startup from `ADMIN_EMAIL` / `ADMIN_PASSWORD`
  (dev defaults `admin@caresphere.dev` / `Admin12345!`); the password is never logged.

```bash
# Example: log in as the bootstrap admin
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@caresphere.dev","password":"Admin12345!"}'
```

## Prerequisites

- Java 17
- Maven 3.9+ (or use the included Maven wrapper)
- Docker (for the local database and integration tests)

## Run locally

```bash
# 1. Start PostgreSQL 16 + pgvector
docker compose up -d

# 2. Run the app
./mvnw spring-boot:run
# API:          http://localhost:8080/api/health
# Swagger UI:   http://localhost:8080/swagger-ui.html
```

## Tests

```bash
./mvnw test          # unit tests (no Docker required)
./mvnw verify        # unit + Testcontainers integration tests (Docker required)
```

## Configuration

Defaults target the `docker-compose.yml` stack. Override via environment variables
(see `.env.example`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`,
`CORS_ALLOWED_ORIGINS`. In production, secrets are loaded from AWS Secrets Manager
(added in the deployment milestone) and never logged.

## License

MIT — educational use.
