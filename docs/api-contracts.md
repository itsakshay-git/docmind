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
  "password": "Password123",
  "fullName": "Akshay"
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
DELETE /api/v1/notebooks/{id}
```

Create request:

```json
{
  "title": "Java Notes"
}
```

## Documents

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

The frontend uses `topK: 5` by default. The control is intentionally hidden from normal users.

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
