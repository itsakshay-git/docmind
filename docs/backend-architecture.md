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

## Feature Packages

```text
auth/       Registration, login, user persistence
notebook/   Notebook ownership and CRUD
document/   PDF upload, parsing, document metadata
ai/         Embedding generation support
rag/        Semantic search and grounded answers
chat/       Persistent notebook chat sessions/messages
security/   JWT filter and security configuration
common/     Shared base entities and error handling
user/       Profile and password settings
```

## Current RAG Flow

1. User uploads a PDF to a notebook.
2. Backend stores document metadata.
3. PDFBox extracts text.
4. Text is chunked.
5. Gemini creates one embedding per chunk.
6. Embeddings are stored as JSON text.
7. Search embeds the user question.
8. Java cosine similarity ranks notebook chunks.
9. Gemini answers using the retrieved context.

## Chat History Flow

1. Frontend loads `GET /api/v1/chat/notebooks/{notebookId}/messages`.
2. User sends `POST /api/v1/chat/notebooks/{notebookId}/messages`.
3. Backend verifies notebook ownership.
4. Backend creates a default notebook chat session if needed.
5. Backend saves the user message.
6. Backend calls the existing RAG answer service.
7. Backend saves the assistant message and serialized source citations.
8. Frontend renders the persisted conversation.

## Design Rules

- Keep secrets in environment variables.
- Add schema changes through new Flyway migrations.
- Keep Gemini access server-side only.
- Use constructor injection with Lombok `@RequiredArgsConstructor`.
- Keep frontend-facing DTOs explicit.

## Management APIs

The backend exposes basic authenticated settings and management APIs:

- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`
- `PUT /api/v1/users/me/password`
- `GET /api/v1/documents`
- `GET /api/v1/documents/notebooks/{notebookId}`
- `DELETE /api/v1/documents/{documentId}`

Document deletion removes the stored file, chunks, and embeddings. Notebook deletion removes related chat history and documents before deleting the notebook.
