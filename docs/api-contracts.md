# API Contracts

Base URL:

```text
http://localhost:8081
```

Authenticated endpoints require:

```text
Authorization: Bearer <jwt>
```

## Auth

### Register

```http
POST /api/v1/auth/register
```

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

### Login

```http
POST /api/v1/auth/login
```

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

Response:

```json
{
  "accessToken": "<jwt>"
}
```

## Notebooks

```http
POST /api/v1/notebooks
GET /api/v1/notebooks
GET /api/v1/notebooks/{id}
PATCH /api/v1/notebooks/{id}
DELETE /api/v1/notebooks/{id}
```

Create request:

```json
{
  "title": "Java Notes"
}
```

Update request:

```json
{
  "title": "Updated title"
}
```

Deleting a notebook also removes its chat history, uploaded documents, chunks, embeddings, and stored files.

## Sources/Documents

### Upload PDF To Notebook

```http
POST /api/v1/documents/{notebookId}/upload
Content-Type: multipart/form-data
```

Form field:

```text
file=<pdf>
```

Upload extracts text, chunks content, generates Gemini embeddings, and stores vectors as JSON text for the current MVP.

### Add Website To Notebook

```http
POST /api/v1/documents/{notebookId}/web-url
Content-Type: application/json
```

```json
{
  "url": "https://example.com/article"
}
```

The backend fetches readable page text with jsoup, then uses the same chunking and embedding pipeline as PDFs.

### Add YouTube Transcript To Notebook

```http
POST /api/v1/documents/{notebookId}/youtube
Content-Type: application/json
```

```json
{
  "url": "https://www.youtube.com/watch?v=VIDEO_ID"
}
```

The backend imports English transcript text when available. If a transcript is unavailable, the source is saved with `FAILED` status and a `failureReason`.

### Add Pasted YouTube Transcript To Notebook

```http
POST /api/v1/documents/{notebookId}/youtube-transcript
Content-Type: application/json
```

```json
{
  "url": "https://www.youtube.com/watch?v=VIDEO_ID",
  "title": "Optional title",
  "transcript": "Paste transcript text here..."
}
```

This is the recommended reliable YouTube path. Auto-fetch is best effort because public YouTube transcript access is inconsistent outside Google products.

### List My Documents

```http
GET /api/v1/documents
```

### List Notebook Documents

```http
GET /api/v1/documents/notebooks/{notebookId}
```

### Delete Document

```http
DELETE /api/v1/documents/{documentId}
```

Deleting a document also removes its chunks, embeddings, and stored file.

Document responses include `sourceType`, `sourceUrl`, `status`, and optional `failureReason`. `sourceType` can be `PDF`, `WEB_URL`, `YOUTUBE`, or `YOUTUBE_TRANSCRIPT`.

### Count Embeddings

```http
GET /api/v1/embeddings/count
```

## RAG

### Semantic Search

```http
POST /api/v1/rag/notebooks/{notebookId}/search
```

```json
{
  "question": "What is this document about?",
  "topK": 5
}
```

### One-Off Ask

```http
POST /api/v1/rag/notebooks/{notebookId}/ask
```

```json
{
  "question": "Explain the key ideas.",
  "topK": 5
}
```

## Chat History

### List Notebook Messages

```http
GET /api/v1/chat/notebooks/{notebookId}/messages
```

Response:

```json
[
  {
    "id": "uuid",
    "role": "USER",
    "content": "What is this notebook about?",
    "sources": [],
    "createdAt": "2026-06-12T10:30:00"
  }
]
```

### Send Notebook Message

```http
POST /api/v1/chat/notebooks/{notebookId}/messages
```

```json
{
  "content": "What are the most important ideas?",
  "topK": 5
}
```

`topK` controls how many retrieved source chunks are used as chat context.

### Clear Notebook Messages

```http
DELETE /api/v1/chat/notebooks/{notebookId}/messages
```

## Studio Artifacts

Studio artifacts are authenticated and notebook-owner scoped.

```http
GET /api/v1/studio/notebooks/{notebookId}/artifacts
GET /api/v1/studio/artifacts/{artifactId}
POST /api/v1/studio/notebooks/{notebookId}/artifacts
DELETE /api/v1/studio/artifacts/{artifactId}
GET /api/v1/studio/artifacts/{artifactId}/download?format=markdown
GET /api/v1/studio/artifacts/{artifactId}/download?format=json
GET /api/v1/studio/artifacts/{artifactId}/download?format=audio
GET /api/v1/studio/artifacts/{artifactId}/download?format=png
GET /api/v1/studio/artifacts/{artifactId}/download?format=jpg
GET /api/v1/studio/artifacts/{artifactId}/audio
GET /api/v1/studio/artifacts/{artifactId}/image
```

Generate request:

```json
{
  "type": "FLASHCARDS",
  "instruction": "Focus on Java primitive data types"
}
```

Supported `type` values:

```text
FLASHCARDS
QUIZ
BRIEFING
PODCAST_SCRIPT
INFOGRAPHIC_OUTLINE
```

Response:

```json
{
  "id": "uuid",
  "notebookId": "uuid",
  "type": "FLASHCARDS",
  "title": "Java Primitive Types Flashcards",
  "markdownContent": "## ...",
  "jsonContent": "{...}",
  "sourceChunkIds": ["uuid"],
  "audioAvailable": false,
  "imageAvailable": false,
  "createdAt": "2026-06-12T10:30:00"
}
```

All artifacts store Markdown and JSON internally. The frontend treats flashcards, quizzes, and briefings as in-app experiences instead of download files. Podcast artifacts attempt Gemini TTS audio generation and expose audio playback/download when `audioAvailable` is `true`. Infographic artifacts render a server-side PNG under `storage/studio-images/`, expose authenticated image preview, and support PNG/JPG download when `imageAvailable` is `true`.

Response stores and returns both sides of the exchange:

```json
{
  "userMessage": {
    "id": "uuid",
    "role": "USER",
    "content": "What are the most important ideas?",
    "sources": [],
    "createdAt": "2026-06-12T10:30:00"
  },
  "assistantMessage": {
    "id": "uuid",
    "role": "ASSISTANT",
    "content": "The document explains...",
    "sources": [
      {
        "chunkId": "uuid",
        "documentId": "uuid",
        "score": 0.82
      }
    ],
    "createdAt": "2026-06-12T10:30:04"
  }
}
```

The frontend exposes this as a compact `Context` selector. Balanced mode sends `topK: 5`.

## User Settings

### Get Profile

```http
GET /api/v1/users/me
```

### Update Profile

```http
PATCH /api/v1/users/me
```

```json
{
  "fullName": "Akshay"
}
```

### Update Password

```http
PUT /api/v1/users/me/password
```

```json
{
  "currentPassword": "old-password",
  "newPassword": "new-password"
}
```
