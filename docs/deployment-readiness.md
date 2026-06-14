# Deployment Readiness

This document prepares DocMind for deployment without deploying it yet.

For step-by-step Vercel, Render, and Neon setup, see `docs/platform-deployment-runbook.md`.

## Recommended First Deployment

For a simple resume-friendly deployment:

```text
Frontend: Vercel
Backend: Render
Database: Neon Postgres
Files: backend filesystem for MVP, object storage later
```

This keeps setup manageable while still showing a real full-stack deployment story.

## Backend Environment Variables

Required:

```text
GEMINI_API_KEY
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
DOCMIND_CORS_ALLOWED_ORIGINS
```

Example:

```text
GEMINI_API_KEY=...
SPRING_DATASOURCE_URL=jdbc:postgresql://your-neon-host.neon.tech/docmind?sslmode=require
SPRING_DATASOURCE_USERNAME=docmind_owner
SPRING_DATASOURCE_PASSWORD=...
DOCMIND_CORS_ALLOWED_ORIGINS=https://your-docmind-app.vercel.app
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

## Frontend Environment Variables

Required for production:

```text
VITE_DOCMIND_API_URL
```

Example:

```text
VITE_DOCMIND_API_URL=https://your-docmind-api.onrender.com
```

## CORS Contract

The backend reads allowed browser origins from:

```text
DOCMIND_CORS_ALLOWED_ORIGINS
```

Use a comma-separated list when more than one deployed frontend origin is needed:

```text
DOCMIND_CORS_ALLOWED_ORIGINS=https://app.example.com,https://preview.example.com
```

Do not use `*` with credentialed JWT requests.

## Database

Production should use managed PostgreSQL. Local Docker PostgreSQL is development-only.

Flyway runs on backend startup and applies migrations automatically. Before a production deploy, confirm:

- The database URL points to the production database.
- The database user has permission to create/alter tables.
- Old migrations are not edited after deployment.
- New schema changes use new Flyway migration versions only.

## File Storage

Current Studio files:

- Podcast audio: `storage/studio-audio`
- Infographic images: `storage/studio-images`

This is acceptable for local MVP demos. For production, platforms without persistent disks may lose these files on restart/redeploy.

Production options:

- Render persistent disk for short-term MVP.
- Cloudflare R2, AWS S3, Supabase Storage, or similar object storage for durable file storage.

Object storage is a future milestone, not part of the current deployment readiness step.

## Health Check

The backend exposes:

```text
GET /actuator/health
```

Use this as the platform health check path.

Expected response:

```json
{
  "status": "UP"
}
```

## Build Commands

Backend:

```bash
cd backend/docmind-api
./mvnw test
./mvnw package -DskipTests
```

Backend Docker image:

```bash
docker build -t docmind-api ./backend/docmind-api
```

Run the backend image against a database reachable from the container:

```bash
docker run --rm -p 8081:8081 \
  -e GEMINI_API_KEY=... \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/docmind \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=admin123 \
  -e DOCMIND_CORS_ALLOWED_ORIGINS=http://127.0.0.1:5173 \
  docmind-api
```

For managed deployment platforms, set the same environment variables in the platform dashboard.

Render Blueprint config lives at:

```text
render.yaml
```

Frontend:

```bash
cd frontend
corepack enable
pnpm install --frozen-lockfile
pnpm build
```

Vercel config lives at:

```text
frontend/vercel.json
```

## Pre-Deploy Checklist

1. CI passes on GitHub.
2. Backend has production database env vars set.
3. Backend has `GEMINI_API_KEY` set.
4. Backend has `DOCMIND_CORS_ALLOWED_ORIGINS` set to the deployed frontend URL.
5. Frontend has `VITE_DOCMIND_API_URL` set to the deployed backend URL.
6. Backend health check returns `UP`.
7. Register/login works.
8. Notebook create/delete works.
9. PDF or website source ingestion works.
10. Chat can answer from indexed sources.
11. Studio can generate at least one artifact.

## Not Included Yet

- Prometheus/Grafana monitoring.
- Object storage for Studio files.
- Docker image publishing.
- Kubernetes.
- CDN/custom domain setup.
- Production rate limiting.
