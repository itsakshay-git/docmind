# Platform Deployment Runbook

This runbook describes a practical first deployment path for DocMind:

```text
Frontend: Vercel
Backend: Render
Database: Neon Postgres
```

It assumes the code is already pushed to GitHub and CI is passing.

## 1. Neon Postgres

Create the production database first so the backend has a real database URL during deployment.

Steps:

1. Create a Neon project.
2. Create or use the default PostgreSQL database.
3. Copy the JDBC connection string.
4. Make sure the URL includes SSL.

Expected JDBC shape:

```text
jdbc:postgresql://<host>/<database>?sslmode=require
```

Backend environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/<database>?sslmode=require
SPRING_DATASOURCE_USERNAME=<neon-user>
SPRING_DATASOURCE_PASSWORD=<neon-password>
```

Flyway runs automatically when the backend starts. Do not manually create DocMind tables in Neon.

## 2. Render Backend

Recommended first backend deploy: Render Web Service from the backend Dockerfile.

Setup:

```text
New Render service: Web Service
Source: GitHub repository
Runtime: Docker
Root directory / Docker build context: backend/docmind-api
Dockerfile path: backend/docmind-api/Dockerfile
Health check path: /actuator/health
```

Required environment variables:

```text
GEMINI_API_KEY=<gemini-key>
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/<database>?sslmode=require
SPRING_DATASOURCE_USERNAME=<neon-user>
SPRING_DATASOURCE_PASSWORD=<neon-password>
DOCMIND_CORS_ALLOWED_ORIGINS=https://<your-vercel-app>.vercel.app
```

Optional demo/account variables:

```text
DOCMIND_DEMO_ENABLED=true
DOCMIND_DEMO_EMAIL=recruiter@docmind.dev
DOCMIND_DEMO_PASSWORD=<strong-demo-password>
DOCMIND_DEMO_FULL_NAME=Recruiter Demo
```

Optional Studio variables:

```text
DOCMIND_STUDIO_TTS_MODEL=gemini-3.1-flash-tts-preview
DOCMIND_STUDIO_TTS_HOST_A_VOICE=Charon
DOCMIND_STUDIO_TTS_HOST_B_VOICE=Aoede
DOCMIND_STUDIO_AUDIO_STORAGE_DIR=storage/studio-audio
DOCMIND_STUDIO_IMAGE_STORAGE_DIR=storage/studio-images
```

Render should expose the backend URL after deploy:

```text
https://<your-render-service>.onrender.com
```

Smoke check:

```text
GET https://<your-render-service>.onrender.com/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

## 3. Vercel Frontend

Deploy the React/Vite frontend after the backend URL is known.

Setup:

```text
New Vercel project
Source: GitHub repository
Framework preset: Vite
Root directory: frontend
Install command: corepack enable && pnpm install --frozen-lockfile
Build command: pnpm build
Output directory: dist
```

Required environment variable:

```text
VITE_DOCMIND_API_URL=https://<your-render-service>.onrender.com
```

After saving the environment variable, redeploy the frontend so Vite bakes the backend URL into the production build.

## 4. CORS Update

After Vercel gives the production frontend URL, update Render:

```text
DOCMIND_CORS_ALLOWED_ORIGINS=https://<your-vercel-app>.vercel.app
```

If you also need preview deployments:

```text
DOCMIND_CORS_ALLOWED_ORIGINS=https://<your-vercel-app>.vercel.app,https://<preview-url>.vercel.app
```

Do not use `*` because DocMind sends authenticated JWT requests.

## 5. Post-Deploy Smoke Tests

Run these in order:

1. Backend health:

```text
GET https://<your-render-service>.onrender.com/actuator/health
```

2. Open frontend:

```text
https://<your-vercel-app>.vercel.app
```

3. Register or log in with the demo account.
4. Create a notebook.
5. Add a website source.
6. Confirm the source status becomes indexed.
7. Ask a chat question based on the source.
8. Generate one Studio artifact.
9. Delete the test notebook.

## 6. Known MVP Limits

- Studio audio and image files are stored on the backend filesystem.
- Some free hosting plans may sleep, causing the first request to be slow.
- Gemini API quota can block chat, embedding, or Studio generation if exhausted.
- YouTube auto-transcript remains best effort; pasted transcript is more reliable.
- Object storage, structured production logs, and Prometheus/Grafana are later milestones.
