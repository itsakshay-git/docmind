# Backend Architecture

The backend is a Spring Boot modular monolith in:

```text
backend/docmind-api/
```

## Stack

- Java 21
- Spring Boot 3.5.14
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring AI 1.1.7
- Google Gemini chat and embeddings
- Apache PDFBox
- jsoup
- Spring Boot Actuator

## Feature Packages

```text
auth/       Registration, login, user persistence
notebook/   Notebook ownership and CRUD
document/   PDF, web URL, and YouTube source ingestion
ai/         Embedding generation support
rag/        Semantic search and grounded answers
chat/       Persistent notebook chat sessions/messages
studio/     Saved study artifacts generated from notebook context
security/   JWT filter and security configuration
common/     Shared base entities and error handling
user/       Profile and password settings
```

## Current Source-To-RAG Flow

1. User adds a source to a notebook: PDF, website URL, or YouTube link.
2. Backend stores source metadata in `documents`.
3. Source-specific extractor produces text.
4. Text is chunked.
5. Gemini creates one embedding per chunk.
6. Embeddings are stored in PostgreSQL `pgvector` as 3072-dimensional vectors, with the legacy JSON text value retained for compatibility.
7. Search embeds the user question.
8. PostgreSQL exact cosine similarity ranks notebook-owned chunks.
9. Gemini answers using the retrieved context.

PDF uses PDFBox. Websites use jsoup. YouTube auto-fetch is best effort. The reliable YouTube path is pasted transcript ingestion, stored as `YOUTUBE_TRANSCRIPT`.

## Chat History Flow

1. Frontend loads `GET /api/v1/chat/notebooks/{notebookId}/messages`.
2. User sends `POST /api/v1/chat/notebooks/{notebookId}/messages`.
3. Backend verifies notebook ownership.
4. Backend creates a default notebook chat session if needed.
5. Backend saves the user message.
6. Backend calls the existing RAG answer service.
7. Backend saves the assistant message and serialized source citations.
8. Frontend renders the persisted conversation.

Current behavior: persisted chat history remains in DocMind tables, and the notebook chat answer path passes only a bounded window of recent notebook-owned turns into retrieval and prompt context. This helps follow-up questions without sending the entire conversation to the model.

Streaming flow:

1. Frontend sends `POST /api/v1/chat/notebooks/{notebookId}/messages/stream`.
2. Backend saves the user message and starts a server-sent events response.
3. Backend streams assistant tokens as they arrive from Spring AI/Gemini.
4. Backend emits source citations and persists the final assistant message when generation completes.
5. Frontend progressively renders tokens and reconciles with the persisted final message.

## Studio Artifact Flow

1. User chooses an artifact type in the notebook Studio panel.
2. Backend verifies notebook ownership.
3. Backend reuses notebook-scoped semantic retrieval to gather source chunks.
4. Gemini generates a Markdown preview and structured JSON payload.
5. Backend stores the artifact in `studio_artifacts`.
6. Podcast artifacts also attempt Gemini TTS with two configured host voices and save WAV audio through `StudioMediaStorage`; the default adapter writes under `storage/studio-audio`.
7. Infographic artifacts render PNG bytes server-side with `BufferedImage` / `Graphics2D` and save them through `StudioMediaStorage`; the default adapter writes under `storage/studio-images`.
8. Frontend opens artifacts as mini apps with state, playback/download where relevant, and delete.

Studio supports flashcards, quiz, briefing, podcast audio/script, and generated infographic images. Deleting a Studio artifact also removes related audio/image media through the storage adapter when present.

## Design Rules

- Keep secrets in environment variables.
- Add schema changes through new Flyway migrations.
- Keep Gemini access server-side only.
- Use constructor injection with Lombok `@RequiredArgsConstructor`.
- Keep frontend-facing DTOs explicit.
- Run the backend test baseline from `docs/quality-checks.md` before committing backend changes.

## Monitoring

Spring Boot Actuator exposes the local/dev monitoring baseline:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

Custom AI/RAG metrics are emitted through Micrometer and appear in `/actuator/prometheus`:

- `docmind_ai_operation_duration_seconds`: operation latency tagged by `operation`, `variant`, and `status`.
- `docmind_ai_operation_errors_total`: operation failures tagged by `operation`, `variant`, and exception class.
- `docmind_ai_operation_items`: item counts such as generated embedding chunks, RAG result counts, and Studio context chunks.

Current instrumented operations include embedding generation, RAG search, non-streaming answer generation, streaming answer completion, and Studio artifact generation.

Only `GET /actuator/health` is public. It is intended for local health checks and future deployment probes. Other exposed actuator endpoints remain behind normal security, and sensitive actuator endpoints are not exposed.

Local infrastructure and environment variables are documented in `docs/local-development.md`.

Deployment readiness, including production datasource and CORS environment variables, is documented in `docs/deployment-readiness.md`.

## Management APIs

The backend exposes basic authenticated settings and management APIs:

- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`
- `PUT /api/v1/users/me/password`
- `DELETE /api/v1/users/me`
- `GET /api/v1/documents`
- `GET /api/v1/documents/notebooks/{notebookId}`
- `POST /api/v1/documents/{notebookId}/web-url`
- `POST /api/v1/documents/{notebookId}/youtube`
- `POST /api/v1/documents/{notebookId}/youtube-transcript`
- `DELETE /api/v1/documents/{documentId}`
- `GET /api/v1/studio/notebooks/{notebookId}/artifacts`
- `POST /api/v1/studio/notebooks/{notebookId}/artifacts`
- `GET /api/v1/studio/artifacts/{artifactId}`
- `GET /api/v1/studio/artifacts/{artifactId}/download`
- `DELETE /api/v1/studio/artifacts/{artifactId}`

Streaming chat API:

- `POST /api/v1/chat/notebooks/{notebookId}/messages/stream`

Document deletion removes the stored file, chunks, and embeddings. Notebook deletion removes related chat history and documents before deleting the notebook.
