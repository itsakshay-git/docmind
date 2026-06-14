# Quality Checks

Run these before committing changes that affect the related area.

## Backend

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
