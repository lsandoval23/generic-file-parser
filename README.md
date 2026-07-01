# File Parser API

A Spring Boot service that ingests reservation and payment files in multiple formats — **XLSX, CSV, JSON, and XML** — validates and maps their columns, and persists the parsed records to PostgreSQL for downstream analytics.

Uploads are processed **asynchronously in batches**, so large files don't block the request thread and partial progress is committed as it goes. The mapping between a source file's columns and the internal data model is **configurable per file type and format**, so onboarding a new client's export format is a data change, not a code change.

For a deeper look at the internals — design patterns, processing pipeline, database schema — see [ARCHITECTURE.md](ARCHITECTURE.md).

## Features

- **Multi-format parsing**: XLSX (streaming, via Apache POI), CSV (Apache Commons CSV), JSON (Jackson streaming), XML (StAX, XXE-protected)
- **Configurable column mapping**: source column/field names are mapped to internal DTO fields per `(file type, file extension)`, stored in the `column_mapping` table and editable via REST
- **Async batch processing**: files are parsed and persisted in batches (default 1000 records), with job status tracked end-to-end
- **N+1-safe persistence**: referenced entities (clients, studios, rooms, disciplines, instructors) are preloaded into an in-memory cache per batch
- **JWT authentication** with permission-based endpoint authorization
- **Job tracking**: query processing jobs by date range and status

## Tech Stack

- Java 17, Spring Boot 3.5.5 (Web, Data JPA, Security, Validation)
- PostgreSQL 16
- Apache POI + xlsx-streamer, Apache Commons CSV, Jackson, StAX
- JJWT for JSON Web Tokens
- Maven

## Running Locally

### 1. Start PostgreSQL

```bash
docker compose -f file-parser-docker/docker-compose.yml up
```

This starts Postgres on `localhost:5432` (db `parser-db`, user/password `admin`/`admin`) and pgAdmin on `localhost:5050`. On first start, `file-parser-docker/init/init.sql` seeds the schema, the `column_mapping` rows for every supported file type/format, and a default admin user.

### 2. Set environment variables

| Variable | Description | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/parser-db` |
| `SPRING_DATASOURCE_USERNAME` | DB user | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `admin` |
| `JWT_SECRET` | Secret used to sign JWTs | any sufficiently long random string |
| `CORS_ALLOWED_ORIGINS` | Optional, comma-separated | defaults to `http://localhost:3000` |

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080/file-parser/api`.

### Running with Docker end-to-end

The root `Dockerfile` builds the app itself (multi-stage Maven build → JRE runtime). Combine it with the compose file above if you want the API containerized too.

## Basic Workflow

1. **Log in** to get a JWT (default seeded user: `admin` / `admin123`):

   ```bash
   curl -X POST http://localhost:8080/file-parser/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

2. **Upload a file** for parsing (reservations or payments):

   ```bash
   curl -X POST http://localhost:8080/file-parser/api/files/upload/reservations \
     -H "Authorization: Bearer <token>" \
     -F "file=@docs/reservations-sample.csv"
   ```

   Returns `202 Accepted` with a job ID immediately; parsing and persistence happen in the background.

3. **Check job status**:

   ```bash
   curl -G http://localhost:8080/file-parser/api/files/jobs \
     -H "Authorization: Bearer <token>" \
     --data-urlencode "from=2025-01-01" \
     --data-urlencode "to=2025-12-31"
   ```

4. **Inspect or edit column mappings** (e.g. to onboard a new source format):

   ```bash
   curl http://localhost:8080/file-parser/api/column-mapping/file-type/RESERVATION \
     -H "Authorization: Bearer <token>"
   ```

### Sample files

Ready-to-upload sample files live in [`docs/`](docs/):

- `reservations-sample.{csv,json,xml}`
- `payments-sample.{csv,json,xml}`

## Project Layout

```
src/main/java/org/lsandoval/fileparser/
├── auth/           JWT auth, users/roles/permissions
├── cache/          In-memory entity cache used during batch persistence
├── dao/            JPA entities + Spring Data repositories
├── exception/      Global exception handling
├── service/
│   ├── parser/     FileParser strategy implementations (xlsx/csv/json/xml)
│   ├── impl/       Business logic (file processing, batch persistence, jobs)
│   └── model/      DTOs
└── web/            REST controllers
```
