# DocMind Roadmap

## Done

- Spring Boot 3 backend with Java 21.
- PostgreSQL and Flyway migrations.
- JWT authentication.
- Notebook CRUD.
- PDF upload into notebook scope.
- PDF text extraction with PDFBox.
- Chunk generation.
- Gemini embedding generation.
- Embedding persistence with PostgreSQL `pgvector` exact cosine search and legacy JSON text compatibility.
- Notebook-scoped semantic search in PostgreSQL.
- Gemini-grounded one-off RAG answers.
- React, TypeScript, Vite frontend.
- Dark/light DocMind workspace UI.
- Persistent notebook chat history.
- Bounded conversational memory for follow-up chat questions.
- Streaming notebook chat endpoint and progressive frontend rendering.
- User-safe AI provider error classification for quota, rate-limit, and transient failures.
- Basic settings page for profile, password, notebooks, and documents.
- Notebook library hover actions for deleting notebooks.
- Notebook title editing from library cards.
- Workspace source list with document deletion.
- Website URL source ingestion.
- YouTube transcript source ingestion.
- Manual pasted YouTube transcript ingestion for reliable demos.
- Studio mini apps: flashcards, quiz, briefing, podcast audio/script, and generated infographic images.
- Studio artifact preview, podcast audio download, infographic PNG/JPG download, playback, and delete.
- Local Docker PostgreSQL healthcheck.
- Spring Boot Actuator health/info/metrics baseline.
- GitHub Actions CI for backend and frontend checks.
- Deployment readiness docs and environment-driven backend database/CORS config.
- Testcontainers backend smoke test for PostgreSQL, Flyway, JPA, and app context startup.
- Backend multi-stage Dockerfile for container deployment.
- Render and Vercel deployment config templates.
- Prometheus-ready Actuator metrics endpoint plus custom AI/RAG operation metrics.
- Pgvector RAG upgrade merged into `main` with exact PostgreSQL cosine search.
- Cloudflare R2 Studio media adapter for durable production audio/image storage.

## Current Milestone

Wind down the deployed v1 into a portfolio-ready project: keep the core demo stable, polish the frontend, preserve production pgvector/R2 verification, and leave deeper retrieval/observability work as future backlog.

Detailed post-v1 backlog:

```text
docs/next-improvements.md
```

## Next Milestones

### Conversational Chat

- Keep full persisted chat history in DocMind's `chat_sessions` and `chat_messages` tables.
- Harden bounded chat memory with regression coverage for follow-up questions.
- Keep memory notebook-scoped and owner-scoped.
- Harden streaming chat UX, cancellation behavior, and error recovery while preserving final assistant persistence.

### Better Retrieval

- Keep exact PostgreSQL `pgvector` search healthy and notebook-owner scoped.
- Keep notebook ownership checks on every retrieval path.
- Add conversation-aware query rewriting, chunk metadata, source previews, hybrid search, reranking, ANN indexing when justified, and re-indexing.

### Source Management

- Add Brave Search import using `BRAVE_SEARCH_API_KEY`.
- Improve source details in the sidebar.
- Add source preview/snippets.
- Support re-indexing notebooks after source changes.

### Production Hardening

- Add bounded retry/backoff policies for transient AI failures.
- Add structured logging.
- Keep the R2 storage smoke test in the production runbook and periodically verify media after redeploys.
- Add Grafana dashboards or hosted observability for the custom AI/RAG metrics after deployment.
- Add production compose if needed.

### Studio V2

- Add PDF export for Studio artifacts.
- Improve infographic rendering templates and add richer downloadable study formats.
- Add regenerate actions for failed podcast audio or image generation.

