# Quality Checks

Run these before committing changes that affect the related area.

## Backend

Backend tests include a Testcontainers PostgreSQL smoke test using `pgvector/pgvector:pg16`. Docker Desktop must be running, but the tests do not use your local `docmind` database.

```powershell
cd "D:\my projects\docmind\backend\docmind-api"
cmd /c mvnw.cmd test
```

Start local PostgreSQL only when manually running the backend app:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

Current backend baseline:

- Tests run: 28
- Failures: 0
- Errors: 0
- Skipped: 0

The Spring context smoke test starts PostgreSQL with Testcontainers, applies Flyway migrations including the `vector` extension migration, validates JPA startup, confirms the demo user can be persisted, and includes pgvector retrieval coverage.

## Frontend

```powershell
cd "D:\my projects\docmind\frontend"
corepack pnpm lint
corepack pnpm format:check
corepack pnpm test
corepack pnpm build
```

Use `corepack pnpm format` to apply the shared Prettier style.

## Monitoring Smoke Check

With the backend running:

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

`/actuator/health` is public for local/dev checks. Other actuator endpoints remain protected unless explicitly permitted in a later deployment milestone.

Prometheus metrics are exposed for authenticated/local monitoring integrations:

```powershell
Invoke-RestMethod http://localhost:8081/actuator/prometheus
```

Do not make `/actuator/prometheus` public on production free-tier hosting unless a controlled scraper requires it.

## GitHub Actions

The CI workflow lives at:

```text
.github/workflows/ci.yml
```

It runs on pushes to `main`, `master`, and `codex/**`, plus pull requests into `main` or `master`.

Backend job:

```powershell
cd "D:\my projects\docmind\backend\docmind-api"
cmd /c mvnw.cmd test
```

Frontend job:

```powershell
cd "D:\my projects\docmind\frontend"
corepack pnpm lint
corepack pnpm format:check
corepack pnpm test
corepack pnpm build
```

The current CI baseline does not require `GEMINI_API_KEY` or a manually configured PostgreSQL service container. Backend integration tests use Testcontainers and GitHub-hosted Docker.

## Production Pgvector Smoke Check

After deploying a pgvector-backed backend to Render with Neon Postgres:

1. In Neon SQL editor, confirm:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
SELECT extversion FROM pg_extension WHERE extname = 'vector';
SELECT to_regtype('vector')::text;
```

Expected: `to_regtype` returns `vector`.

2. Confirm backend health:

```powershell
Invoke-RestMethod https://docmind-2fa9.onrender.com/actuator/health
```

3. Open the Vercel frontend, upload or paste a source, ask a source-grounded chat question, ask one follow-up question, and generate one Studio artifact.
