# DocMind Documentation Map

This folder is the project memory for DocMind. Production-facing docs now live in `current/`; older handoff and refactor notes live in the ignored `archive/` folder for local reference only.

## Start Here

- `../README.md`
  Recruiter-facing project overview with live links, screenshots, system design preview, stack, and quality checks.
- `current/system-design.md`
  Detailed deployed system design: architecture, flows, data model, security, deployment, monitoring, tradeoffs, and future improvements.
- `assets/system-design.svg`
  Openable system design diagram used by the README and system design doc.

## Current Engineering Docs

- `current/architecture.md`
  Short architecture overview and source-to-answer flow.
- `current/backend-architecture.md`
  Backend module map, RAG flow, Studio flow, management APIs, and monitoring notes.
- `current/frontend-architecture.md`
  Frontend folder structure, state model, route/page ownership, and UI behavior.
- `current/database-schema.md`
  Database tables and schema notes.
- `current/api-contracts.md`
  Backend API contract reference.
- `current/api-quickstart.md`
  Practical API examples for login, notebooks, sources, chat, Studio, and settings.

## Run, Test, Deploy

- `current/local-development.md`
  Local Docker PostgreSQL, backend/frontend run commands, environment variables, and Actuator checks.
- `current/quality-checks.md`
  Backend/frontend verification commands, CI notes, Testcontainers baseline, and monitoring smoke checks.
- `current/deployment-readiness.md`
  Production environment contract, CORS, file storage notes, health checks, and deployment checklist.
- `current/platform-deployment-runbook.md`
  Step-by-step Vercel, Render, Neon, and Cloudflare R2 deployment guide.

## Product Planning

- `current/roadmap.md`
  Completed work, current direction, and future milestones.
- `current/next-improvements.md`
  Prioritized post-v1 backlog for retrieval, chat, Studio, quota handling, and observability improvements.
- `current/decisions.md`
  Architecture decision record. Use this to explain why core technical choices were made.

## Local Archive

`archive/` is intentionally ignored by Git. It can hold historical handoffs, refactor snapshots, and local notes that are useful while working but should not be treated as production documentation.

## Asset Folders

- `assets/screenshots/`
  README screenshots.
- `assets/system-design.svg`
  System design diagram.

Prefer adding new visual assets under `assets/screenshots/` or a clearly named subfolder.