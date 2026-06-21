# DocMind Agent Instructions

## Project Context

DocMind is a deployed full-stack document intelligence platform inspired by NotebookLM. The current MVP supports notebook-scoped source ingestion, text extraction, chunking, Gemini embeddings, semantic retrieval, grounded chat, and Studio study artifact generation.

Start with [README.md](README.md), [docs/README.md](docs/README.md), and [docs/system-design.md](docs/system-design.md). Use [docs/chatgpt-project-handoff.md](docs/chatgpt-project-handoff.md) only as historical project context.

## Stack

- Java 21
- Spring Boot 3.5.14
- Maven
- PostgreSQL
- Flyway
- Spring Security with JWT
- Spring AI 1.1.7
- Google Gemini for chat, embeddings, Studio generation, and TTS
- Apache PDFBox for PDF parsing
- jsoup for website ingestion
- React, TypeScript, Vite frontend
- TanStack Query for frontend server state
- Docker, GitHub Actions CI, Testcontainers, and Spring Boot Actuator

## Working Rules

- Before making changes, inspect the relevant existing files and follow local patterns.
- Keep changes narrow and explain changed files plus diffs in chat.
- Never commit API keys or secrets.
- Use `GEMINI_API_KEY` from the environment for Gemini configuration.
- Use constructor injection with `@RequiredArgsConstructor`.
- Do not use field injection.
- Add database changes through new Flyway migrations only.
- Do not edit old migrations unless the user explicitly asks.
- Prefer feature-based packages such as `ai`, `document`, `rag`, `security`, `common`, and user/auth-related modules.

## Current Direction

DocMind is deployed as a portfolio-ready MVP:

- Frontend: Vercel
- Backend: Render
- Database: Neon PostgreSQL
- AI provider: Google Gemini
- Monitoring baseline: Actuator health plus Prometheus-ready metrics

Near-term work should focus on polish and production hardening:

- Cloudflare R2 object storage is implemented for Studio audio/image files; future work should enable and smoke-test it in production.
- `pgvector` is implemented for exact database-native similarity search; future retrieval work should focus on ANN indexing when justified, hybrid search, reranking, source previews, and re-indexing.
- Streaming chat is implemented; future work should harden cancellation, retries, and recovery behavior.
- Gemini quota/rate-limit handling is implemented at a user-safe baseline; future work should add dashboards, structured logs, and safe bounded retries.
- Production observability beyond the current Actuator baseline.
