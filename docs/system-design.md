# DocMind System Design

This document explains the deployed DocMind architecture, data flow, storage model, and operational design.

Diagram:

```text
docs/assets/system-design.svg
```

![DocMind system design](assets/system-design.svg)

## 1. Product Goal

DocMind is a notebook-scoped document intelligence platform. A user creates notebooks, adds sources, asks grounded questions, and generates study artifacts from the indexed notebook content.

The system prioritizes:

- Clear notebook ownership boundaries.
- Reliable source ingestion before advanced retrieval.
- Inspectable PostgreSQL-backed persistence.
- Practical deployment using managed platforms.
- A resume-ready architecture that demonstrates full-stack AI, retrieval, persistence, CI, Docker, and cloud deployment.

## 2. High-Level Architecture

```text
Browser
  -> Vercel React frontend
  -> Render Spring Boot API
  -> Neon PostgreSQL
  -> Google Gemini
```

Core runtime responsibilities:

- React manages the workspace UI, auth session, notebook views, chat state, source panels, and Studio mini apps.
- Spring Boot owns authentication, authorization, source ingestion, chunking, embeddings, retrieval, chat persistence, and Studio artifact generation.
- PostgreSQL stores users, notebooks, source metadata, chunks, embeddings, chat history, and Studio artifact metadata.
- Gemini generates embeddings, grounded answers, Studio text artifacts, and podcast audio.

## 3. Frontend Design

The frontend is a Vite React application deployed to Vercel.

Main routes:

- `/` login/register recruiter-facing entry.
- `/notebooks` notebook library.
- `/notebooks/:notebookId` notebook workspace.
- `/settings` account settings.

Main frontend areas:

- Auth feature: login/register, JWT session, demo account display.
- Notebook feature: list, search, sort, create, rename, delete.
- Documents feature: upload PDF, add website, add YouTube source, paste transcript, delete source.
- Chat feature: persisted notebook chat, Markdown/code rendering, context count, clear chat.
- Studio feature: flashcards, quiz, briefing, podcast, infographic mini apps.
- User feature: profile, password, theme, delete account.

State model:

- TanStack Query handles server state and cache invalidation.
- AuthContext owns the JWT session.
- Local React state handles UI-only state such as active mobile tab, open Studio artifact, flashcard progress, quiz score, and form drafts.

## 4. Backend Design

The backend is a Spring Boot modular monolith deployed to Render as a Docker container.

Feature packages:

```text
auth       registration, login, user persistence
notebook   notebook ownership and CRUD
document   PDF, website, YouTube, and transcript ingestion
ai         embedding generation support
rag        semantic search and grounded answers
chat       persisted notebook chat
studio     generated study artifacts
security   JWT filter, CORS, security rules
common     shared entities and errors
user       profile and account settings
```

Key backend rules:

- All notebook APIs are owner-scoped.
- JWT is required for application APIs.
- `/actuator/health` is public for deployment health checks.
- Secrets are read from environment variables.
- Schema changes are applied through Flyway only.

## 5. Source Ingestion Flow

```text
User adds source
  -> backend verifies notebook ownership
  -> document row is created
  -> source-specific extractor produces text
  -> text is chunked
  -> Gemini generates one embedding per chunk
  -> chunks and embeddings are stored
  -> source status is updated
```

Supported source types:

- `PDF`
- `WEB_URL`
- `YOUTUBE`
- `YOUTUBE_TRANSCRIPT`

Extraction strategy:

- PDF uses Apache PDFBox.
- Website sources use jsoup and metadata-aware text cleanup.
- YouTube auto-transcript is best effort.
- Pasted YouTube transcript is the reliable production/demo path.

Failure behavior:

- Source metadata is retained.
- Failed sources store a failure reason.
- Chat and Studio retrieval only benefit from successfully indexed chunks.

## 6. Retrieval And Chat Flow

```text
User asks question
  -> frontend sends notebook-scoped chat request
  -> backend stores user message
  -> question plus bounded chat memory is embedded with Gemini
  -> PostgreSQL searches notebook-owned pgvector embeddings
  -> exact cosine similarity ranks chunks in the database
  -> top chunks are sent to Gemini as grounded context
  -> assistant tokens stream to the frontend
  -> final assistant answer is stored
  -> frontend reconciles the persisted Markdown/code response
```

Current retrieval design:

- Vectors are stored in PostgreSQL `pgvector` as `vector(3072)`, with legacy JSON `TEXT` retained for compatibility.
- Exact cosine similarity runs in PostgreSQL through a narrow JDBC repository.
- Search is restricted to one notebook owned by the authenticated user.
- Default retrieval count is kept small for predictable latency and cost.
- Chat history is persisted for display and deletion, and recent prior turns are passed as bounded model memory during notebook chat answer generation.

Future retrieval upgrade:

- Add conversation-aware query rewriting, chunk metadata, re-indexing, and source previews.
- Evaluate hybrid keyword/vector search, reranking, and ANN indexing after corpus size and latency justify the extra complexity.
- Continue hardening streaming chat, cancellation behavior, and user-facing error recovery while preserving the existing non-streaming API fallback.

## 7. Studio Artifact Flow

```text
User selects Studio artifact
  -> backend retrieves notebook context
  -> Gemini generates structured JSON and Markdown
  -> artifact row is saved
  -> optional media file is generated
  -> frontend opens mini app view
```

Artifact types:

- Flashcards: interactive colored cards with progress state.
- Quiz: interactive answer selection, score, and explanations.
- Briefing: readable Markdown summary.
- Podcast: two-voice audio and visible script.
- Infographic: generated PNG preview with PNG/JPG download.

Persistence:

- Artifact metadata, Markdown, JSON, and source chunk IDs are stored in PostgreSQL.
- Podcast audio is stored through `StudioMediaStorage`; filesystem is the local default and Cloudflare R2 is enabled for durable production media.
- Infographic images are stored through `StudioMediaStorage`; filesystem is the local default and Cloudflare R2 is enabled for durable production media.

Production note:

- Filesystem storage is acceptable for local MVP demos and is isolated behind a storage interface.
- Durable production storage uses the Cloudflare R2 `StudioMediaStorage` adapter with an authenticated smoke test that verifies write/read/delete without calling Gemini.

## 8. Data Model

Main tables:

- `users`
- `notebooks`
- `documents`
- `chunks`
- `embeddings`
- `chat_sessions`
- `chat_messages`
- `studio_artifacts`

Ownership model:

- Users are identified by email.
- Notebooks store `owner_email`.
- Documents, chat sessions, and Studio artifacts are notebook-scoped.
- Service methods verify ownership before reading or mutating notebook data.

Migration model:

- Flyway owns schema creation and changes.
- Old migrations are not edited after deployment.
- New schema changes use new versioned migrations.

## 9. Security Design

Authentication:

- JWT login/register flow.
- Frontend stores the token through the app auth layer.
- API calls attach `Authorization: Bearer <token>`.

Authorization:

- Notebook ownership is checked in backend services.
- RAG, chat, source, and Studio APIs are authenticated.
- Actuator health is public.

CORS:

- Local defaults allow Vite dev origins.
- Production origins are configured through `DOCMIND_CORS_ALLOWED_ORIGINS`.
- Wildcard CORS is avoided because DocMind uses authenticated requests.

Secrets:

- Gemini API key is backend-only.
- Database credentials are backend-only.
- Vercel only receives the public backend base URL.

## 10. Deployment Design

Current deployed shape:

```text
Vercel
  React/Vite frontend: https://docmind-omega-woad.vercel.app/

Render
  Spring Boot Docker container: https://docmind-2fa9.onrender.com

Neon
  Managed PostgreSQL

Google Gemini
  Embeddings, chat, Studio generation, TTS
```

Deployment configs:

- `frontend/vercel.json`
- `render.yaml`
- `backend/docmind-api/Dockerfile`

Health check:

```text
GET /actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

## 11. CI And Quality

GitHub Actions runs:

- Backend Maven tests.
- Frontend lint.
- Frontend format check.
- Frontend tests.
- Frontend production build.

Backend tests include:

- Unit tests for embedding, RAG, YouTube transcript ingestion, and Studio behavior.
- Testcontainers PostgreSQL smoke test for Spring context, Flyway, JPA, and demo user persistence.

## 12. Monitoring Baseline

Current monitoring:

- Spring Boot Actuator.
- Public `/actuator/health`.
- Exposed `info`, `metrics`, and `prometheus` endpoints behind authentication.
- Custom Micrometer metrics track AI/RAG operation duration, errors, and item counts.
- Render health check integration.
- AI provider errors are mapped to user-safe responses, including 429 for quota/rate-limit and 503 for transient provider/network failures.

Future monitoring:

- Grafana dashboard.
- Structured logs.
- Dashboarding and alerting for the custom AI/RAG latency/error metrics.

## 13. Known Tradeoffs

Current MVP decisions:

- Embeddings use exact PostgreSQL `pgvector` search. ANN indexing is deferred because current Gemini embeddings are 3072-dimensional and the corpus is still MVP-sized.
- Chat history is persisted, and bounded recent-turn memory is included in notebook chat prompts.
- Studio files use `StudioMediaStorage`; filesystem is the local default and Cloudflare R2 is enabled and smoke-tested for production media.
- YouTube auto-transcript is best effort.
- Streaming chat now uses a server-sent events endpoint while keeping the non-streaming endpoint as a fallback.

Why these choices are acceptable:

- They reduce moving parts for the first deployed MVP.
- They make behavior easier to inspect and debug.
- They keep the system understandable for interviews.

## 14. Future Improvements

High-value next steps:

- Harden bounded conversational memory for follow-up questions.
- Harden streaming assistant responses and cancellation behavior.
- Add conversation-aware retrieval, source previews, hybrid search, reranking, and re-indexing.
- Continue hardening Studio media UX, failed-media regeneration, and export formats.
- Add Brave Search import.
- Add Prometheus/Grafana or hosted dashboards for the custom AI/RAG metrics.
- Add bounded retry/backoff policies and richer AI error metrics.

Detailed backlog:

```text
docs/next-improvements.md
```

