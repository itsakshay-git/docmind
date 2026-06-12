# DocMind API Quickstart

Base URL:

```text
http://localhost:8081
```

## 1. Register

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "Password123!"
}
```

## 2. Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "Password123!"
}
```

Copy `accessToken` from the response.

For all following requests, add:

```http
Authorization: Bearer <accessToken>
```

## 3. Create Notebook

```http
POST /api/v1/notebooks
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "title": "Java Notes"
}
```

Copy the returned notebook `id`.

## 4. Upload PDF

```http
POST /api/v1/documents/{notebookId}/upload
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

Form field:

```text
file = your PDF file
```

Expected response:

```text
Uploaded
```

## 5. Check Embedding Count

```http
GET /api/v1/embeddings/count
Authorization: Bearer <accessToken>
```

The count should increase after a successful upload.

## 6. Semantic Search

```http
POST /api/v1/rag/notebooks/{notebookId}/search
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "question": "What are Java variable naming rules?",
  "topK": 5
}
```

## 7. Ask Question

```http
POST /api/v1/rag/notebooks/{notebookId}/ask
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "question": "What are Java variable naming rules?",
  "topK": 5
}
```

`topK` is an API-level retrieval setting. The React app hides it and sends `5` by default.

Expected response:

```json
{
  "answer": "Answer generated from retrieved document chunks.",
  "sources": [
    {
      "chunkId": "...",
      "documentId": "...",
      "score": 0.56
    }
  ]
}
```

## 8. Persistent Chat

```http
POST /api/v1/chat/notebooks/{notebookId}/messages
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "content": "Summarize this notebook.",
  "topK": 5
}
```

Load saved messages:

```http
GET /api/v1/chat/notebooks/{notebookId}/messages
Authorization: Bearer <accessToken>
```

## 9. Settings

Profile:

```http
GET /api/v1/users/me
PATCH /api/v1/users/me
PUT /api/v1/users/me/password
```

Documents:

```http
GET /api/v1/documents
GET /api/v1/documents/notebooks/{notebookId}
DELETE /api/v1/documents/{documentId}
```
