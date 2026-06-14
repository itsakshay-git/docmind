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
  -> question is embedded with Gemini
  -> candidate notebook chunks are loaded
  -> stored JSON vectors are parsed
  -> cosine similarity ranks chunks
  -> top chunks are sent to Gemini as grounded context
  -> assistant answer is stored
  -> frontend renders Markdown/code response
```

Current retrieval design:

- Vectors are stored as JSON `TEXT`.
- Similarity runs in Java memory.
- Search is restricted to one notebook owned by the authenticated user.
- Default retrieval count is kept small for predictable latency and cost.

Future retrieval upgrade:

- Move vectors to PostgreSQL `pgvector`.
- Push similarity search into the database.
- Add better chunk metadata, re-indexing, and source previews.

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
- Podcast audio is stored on backend filesystem.
- Infographic images are stored on backend filesystem.

Production note:

- Filesystem storage is acceptable for the MVP.
- Durable production storage should move to S3, Cloudflare R2, Supabase Storage, or similar object storage.

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
- Exposed `info` and `metrics` endpoints behind authentication.
- Render health check integration.

Future monitoring:

- Prometheus metrics endpoint.
- Grafana dashboard.
- Structured logs.
- Request latency/error tracking.

## 13. Known Tradeoffs

Current MVP decisions:

- Embeddings are JSON text, not `pgvector`.
- Java in-memory cosine search is used for retrieval.
- Studio files use backend filesystem storage.
- YouTube auto-transcript is best effort.
- Real-time streaming chat is not implemented yet.

Why these choices are acceptable:

- They reduce moving parts for the first deployed MVP.
- They make behavior easier to inspect and debug.
- They keep the system understandable for interviews.

## 14. Future Improvements

High-value next steps:

- Move embeddings to `pgvector`.
- Add streaming assistant responses.
- Add object storage for Studio media.
- Add Brave Search import.
- Add source preview and re-indexing.
- Add Prometheus/Grafana or hosted observability.
- Add rate-limit friendly AI retry and user-facing quota errors.
