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
6. Embeddings are stored as JSON text.
7. Search embeds the user question.
8. Java cosine similarity ranks notebook chunks.
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

## Studio Artifact Flow

1. User chooses an artifact type in the notebook Studio panel.
2. Backend verifies notebook ownership.
3. Backend reuses notebook-scoped semantic retrieval to gather source chunks.
4. Gemini generates a Markdown preview and structured JSON payload.
5. Backend stores the artifact in `studio_artifacts`.
6. Podcast artifacts also attempt Gemini TTS with two configured host voices and save WAV audio under `storage/studio-audio`.
7. Infographic artifacts render a PNG server-side with `BufferedImage` / `Graphics2D` and save it under `storage/studio-images`.
8. Frontend opens artifacts as mini apps with state, playback/download where relevant, and delete.

Studio supports flashcards, quiz, briefing, podcast audio/script, and generated infographic images. Deleting a Studio artifact also removes related audio/image files when present.

## Design Rules

- Keep secrets in environment variables.
- Add schema changes through new Flyway migrations.
- Keep Gemini access server-side only.
- Use constructor injection with Lombok `@RequiredArgsConstructor`.
- Keep frontend-facing DTOs explicit.
- Run the backend test baseline from `docs/quality-checks.md` before committing backend changes.

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

Document deletion removes the stored file, chunks, and embeddings. Notebook deletion removes related chat history and documents before deleting the notebook.
