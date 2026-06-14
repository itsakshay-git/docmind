# Local Development

This page is the local environment contract for DocMind. It keeps infrastructure setup, environment variables, and basic monitoring checks in one place.

## Local Infrastructure

DocMind uses Docker Compose locally for PostgreSQL only.

```powershell
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

Local database defaults:

```text
Host: localhost
Port: 5433
Database: docmind
User: admin
Password: admin123
```

Check container status:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml ps
```

Stop PostgreSQL:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml down
```

Reset local database data:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml down -v
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

The compose file includes a Postgres healthcheck using `pg_isready`. Keep this compose file focused on local PostgreSQL until a later monitoring or deployment milestone.

## Backend Environment

Required:

```text
GEMINI_API_KEY
```

Optional:

```text
DOCMIND_DEMO_ENABLED
DOCMIND_DEMO_EMAIL
DOCMIND_DEMO_PASSWORD
DOCMIND_DEMO_FULL_NAME
DOCMIND_STUDIO_TTS_MODEL
DOCMIND_STUDIO_TTS_HOST_A_VOICE
DOCMIND_STUDIO_TTS_HOST_B_VOICE
DOCMIND_STUDIO_AUDIO_STORAGE_DIR
DOCMIND_STUDIO_IMAGE_STORAGE_DIR
```

Studio audio and image files are stored on the backend filesystem for the MVP. Production deployment should move these files to object storage or another durable storage service.

Run backend:

```powershell
cd "D:\my projects\docmind\backend\docmind-api"
cmd /c mvnw.cmd spring-boot:run
```

The API runs at:

```text
http://localhost:8081
```

## Frontend Environment

Optional:

```text
VITE_DOCMIND_API_URL=http://localhost:8081
```

Run frontend:

```powershell
cd "D:\my projects\docmind\frontend"
corepack pnpm dev
```

The app runs at:

```text
http://127.0.0.1:5173
```

## Monitoring Baseline

The backend uses Spring Boot Actuator for local/dev health and diagnostics.

Exposed actuator endpoints:

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

Security behavior:

- `GET /actuator/health` is public for local/dev health checks.
- `GET /actuator/info` and `GET /actuator/metrics` are exposed but still require authentication.
- Sensitive actuator endpoints are not exposed.

Manual health check:

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
```

Expected shape:

```json
{
  "status": "UP"
}
```

Prometheus, Grafana, Kubernetes, and cloud deployment are intentionally outside this milestone.

## Local Dev Checklist

1. Start Docker Desktop.
2. Start PostgreSQL:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

3. Set `GEMINI_API_KEY` for the backend run configuration.
4. Start the Spring Boot backend on `http://localhost:8081`.
5. Start the React frontend on `http://127.0.0.1:5173`.
6. Log in with the demo account or a registered user.
7. Add sources, ask questions, and generate Studio artifacts.
8. Run quality checks before committing.
