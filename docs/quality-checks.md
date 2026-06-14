# Quality Checks

Run these before committing changes that affect the related area.

## Backend

Start local PostgreSQL first when running integration-style backend checks:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

```powershell
cd "D:\my projects\docmind\backend\docmind-api"
cmd /c mvnw.cmd test
```

Current backend baseline:

- Tests run: 15
- Failures: 0
- Errors: 0
- Skipped: 1 context smoke test

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
