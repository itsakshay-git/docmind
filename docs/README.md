# DocMind Documentation Map

This folder is the project memory for DocMind. It is intentionally split into public-facing docs, engineering reference docs, deployment docs, and internal project history notes.

## Start Here

- `../README.md`
  Recruiter-facing project overview with live links, screenshots, system design preview, stack, and quality checks.
- `system-design.md`
  Detailed deployed system design: architecture, flows, data model, security, deployment, monitoring, tradeoffs, and future improvements.
- `assets/system-design.svg`
  Openable system design diagram used by the README and system design doc.

## Architecture Reference

- `architecture.md`
  Short architecture overview and source-to-answer flow.
- `backend-architecture.md`
  Backend module map, RAG flow, Studio flow, management APIs, and monitoring notes.
- `frontend-architecture.md`
  Frontend folder structure, state model, route/page ownership, and UI behavior.
- `database-schema.md`
  Database tables and schema notes.
- `api-contracts.md`
  Backend API contract reference.
- `api-quickstart.md`
  Practical API examples for login, notebooks, sources, chat, Studio, and settings.

## Run, Test, Deploy

- `local-development.md`
  Local Docker PostgreSQL, backend/frontend run commands, environment variables, and Actuator checks.
- `quality-checks.md`
  Backend/frontend verification commands, CI notes, Testcontainers baseline, and monitoring smoke checks.
- `deployment-readiness.md`
  Production environment contract, CORS, file storage notes, health checks, and deployment checklist.
- `platform-deployment-runbook.md`
  Step-by-step Vercel, Render, and Neon deployment guide.

## Product Planning

- `roadmap.md`
  Completed work, current direction, and future milestones.
- `next-improvements.md`
  Prioritized post-v1 backlog for conversational memory, streaming chat hardening, stronger RAG beyond exact `pgvector` search, object storage, quota handling, and observability.
- `decisions.md`
  Architecture decision record. Use this to explain why core technical choices were made.

## Internal Engineering Notes

These files are useful project history and AI/coding-agent context. They are not the main recruiter-facing story, but they help future contributors or assistants understand how the codebase evolved.

- `chatgpt-project-handoff.md`
  Historical handoff from the original ChatGPT Project workspace. It is not the current architecture source of truth.
- `frontend-structure-snapshot.md`
  Snapshot of the frontend structure before/through the safe refactor.
- `frontend-refactor-rules.md`
  Guardrails used during the frontend restructuring work.

## Asset Folders

- `assets/screenshots/`
  README screenshots.
- `assets/system-design.svg`
  System design diagram.

Prefer adding new visual assets under `assets/screenshots/` or a clearly named subfolder.
