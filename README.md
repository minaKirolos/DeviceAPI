# Device Management API

A production-quality REST API for managing **Device** resources, built with **Spring Boot 4** and persisted in **MySQL**. Devices have a state lifecycle (`AVAILABLE` / `IN_USE` / `INACTIVE`) and a small set of domain rules that the API enforces.

---

## Tech stack

| Layer | Choice |
|---|---|
| Runtime | Java 21, Spring Boot 4.0.6 |
| Web | Spring MVC, springdoc-openapi (Swagger UI) |
| Persistence | Spring Data JPA, Hibernate 7, MySQL 8 |
| Schema migrations | Flyway (`ddl-auto=validate`) |
| Tests | JUnit 5/6, Mockito, AssertJ, Testcontainers (real MySQL) |
| Packaging | Multi-stage Dockerfile + `docker compose` |

---

## Prerequisites

- **Docker Desktop** (for the quick start and for tests)
- **Java 21+** and **Maven 3.9+** *only if you want to run/build outside Docker*

> The repo includes the Maven wrapper (`mvnw` / `mvnw.cmd`), so no separate Maven install is required for builds.

---

## Quick start — Docker (recommended)

From the project root:

```bash
docker compose up --build
```

This builds the app image, starts a MySQL 8 container with the `devicedb` schema, waits for it to be healthy, then starts the app.

Once up:

- API: <http://localhost:8080/api/devices>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI spec: <http://localhost:8080/v3/api-docs>

To stop:

```bash
docker compose down       # stop and remove containers
docker compose down -v    # also wipe the MySQL data volume
```

> The MySQL container is exposed on host port **3307** (not 3306) to avoid clashing with a local MySQL service. The app inside the compose network still talks to `db:3306`.

---

## Local development (IntelliJ + a local MySQL)

1. Have a local MySQL 8 running and create the schema:
   ```sql
   CREATE DATABASE devicedb;
   ```
2. The app reads connection details from environment variables with sensible defaults (see `application.properties`):
   ```
   DB_HOST=localhost  DB_PORT=3306  DB_NAME=devicedb  DB_USER=root  DB_PASSWORD=root
   ```
   Adjust if your MySQL uses a different password.
3. Run `DeviceApiApplication` in IntelliJ.

On startup, Flyway applies the migration in `src/main/resources/db/migration/`, Hibernate validates the schema, and the app listens on **port 8080**.

---

## API overview

Base path: **`/api/devices`**

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/devices` | Create a device. Returns **201** + `Location` header. |
| `GET` | `/api/devices/{id}` | Fetch one device. **404** if not found. |
| `GET` | `/api/devices?brand=&state=&page=&size=&sort=` | Paged list, optional filters by `brand` and/or `state`. |
| `PUT` | `/api/devices/{id}` | Replace all fields. |
| `PATCH` | `/api/devices/{id}` | Update only provided fields. |
| `DELETE` | `/api/devices/{id}` | Delete. Returns **204**. |

All endpoints are auto-documented in **Swagger UI** with request/response schemas, example bodies, and documented status codes.

### Example: create a device

```bash
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{"name":"Pixel 9","brand":"Google","state":"AVAILABLE"}'
```

Response (`201 Created`, abbreviated):

```json
{
  "id": "8b…",
  "name": "Pixel 9",
  "brand": "Google",
  "state": "AVAILABLE",
  "creationTime": "2026-05-30T08:30:00Z"
}
```

### Paged list shape

```json
{
  "content": [ /* devices */ ],
  "page": { "size": 10, "number": 0, "totalElements": 16, "totalPages": 2 }
}
```

Default page size is **10**; default sort is `creationTime DESC`.

---

## Domain rules (enforced server-side)

1. **Creation time is immutable.** It's set once on insert by the constructor. No setter is exposed.
2. **Name and brand cannot be updated while a device is `IN_USE`.** Attempting to change either field while in use returns **409 Conflict**. Changing only `state` (e.g. releasing the device) is allowed.
3. **`IN_USE` devices cannot be deleted.** Attempting to delete returns **409 Conflict**.

All three rules live in `DeviceService` and are covered by unit tests.

---

## Architecture

Classic layered design:

```
Controller (HTTP + DTOs)
    ↓
Service (business rules, transactions)
    ↓
Repository (Spring Data JPA)
    ↓
MySQL (schema owned by Flyway)
```

- **DTOs** (`CreateDeviceRequest`, `UpdateDeviceRequest`, `PatchDeviceRequest`, `DeviceResponse`) decouple the API contract from the JPA entity. The entity is never exposed to clients.
- **`@RestControllerAdvice`** maps domain exceptions to consistent JSON error responses:
    - `DeviceNotFoundException` → **404**
    - `DeviceInUseException` → **409**
    - validation / malformed body / bad sort property → **400**
- **`UUID` primary key**, stored as `BINARY(16)` for compact indexing.
- **Indexes on `brand` and `state`** for the filter endpoints.

---

## Testing

```bash
./mvnw test
```

Two test layers:

- **Unit tests** (`DeviceServiceTest`) — Mockito mocks the repository, exercises all three domain rules and the not-found paths. Fast; no infrastructure.
- **Integration tests** (`DeviceApiIntegrationTest`) — `@SpringBootTest` + `MockMvc` + **Testcontainers MySQL** (`mysql:8.0`) wired via `@ServiceConnection`. Flyway runs against the throwaway DB, Hibernate validates the schema, and the test drives the real HTTP endpoints to verify status codes, response shapes, and persistence end-to-end.

A shared `TestcontainersConfiguration` ensures every `@SpringBootTest` uses a container — no test touches your dev database.

> Testcontainers requires Docker to be running.

---

## Configuration reference

All knobs live in `application.properties` and are overridable via environment variables:

| Property | Env var | Default |
|---|---|---|
| `spring.datasource.url` host | `DB_HOST` | `localhost` |
| `spring.datasource.url` port | `DB_PORT` | `3306` |
| `spring.datasource.url` database | `DB_NAME` | `devicedb` |
| `spring.datasource.username` | `DB_USER` | `root` |
| `spring.datasource.password` | `DB_PASSWORD` | `root` |

---

## Project layout

```
device-api/
├── src/main/java/com/devicemanagement/device_api/
│   ├── domain/      Device entity + DeviceState enum
│   ├── repository/  DeviceRepository
│   ├── dto/         request/response records
│   ├── mapper/      DeviceMapper
│   ├── service/     DeviceService (domain rules)
│   ├── controller/  DeviceController
│   ├── exception/   custom exceptions + GlobalExceptionHandler
│   └── config/      OpenApiConfig
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/V1__create_device_table.sql
├── src/test/java/...
│   ├── DeviceApiApplicationTests
│   ├── TestcontainersConfiguration
│   ├── service/DeviceServiceTest
│   └── DeviceApiIntegrationTest
├── Dockerfile
├── compose.yaml
└── pom.xml
```

---
## Test coverage

Code coverage is measured by **JaCoCo**, wired into the `test` phase of the build. Running `./mvnw test` (or `./mvnw verify`) generates an HTML report at `target/site/jacoco/index.html` showing per-package, per-class, and line-by-line coverage. The current run reports **83% instruction and 83% branch coverage** across **15 classes** — with 100% on the `domain`, `dto`, `mapper`, and `config` packages, **85%** on the `service` layer (where the domain rules live), and **73%** on the global exception handler.
## Future improvements

Things that are *out of scope* for this submission but would be natural next steps:

- **Mapping** — replace the hand-written `DeviceMapper` with **MapStruct** ( depending on the size of the project ).
- **Observability** — Spring Boot Actuator + Micrometer, structured JSON logging.
- **Security** — authentication/authorization (Spring Security + JWT or OAuth2).
- **Better Swagger UX for `Pageable`** — explicit `page`/`size`/`sort` parameters with documented examples instead of the auto-generated `Pageable` schema.
- **CI pipeline** — GitHub Actions running `mvnw test` and building the Docker image on every push.
- **Uniform 500 fallback** — add a final `@ExceptionHandler(Exception.class)` once we're confident it won't swallow framework exceptions.

---

## License

MIT.