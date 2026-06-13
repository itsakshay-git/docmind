# DocMind API Quickstart

Base URL:

```text
http://localhost:8081
```

## 1. Demo Login

For recruiter/interviewer demos, DocMind seeds a local demo account on startup unless `DOCMIND_DEMO_ENABLED=false` is set.

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "recruiter@docmind.dev",
  "password": "Demo@12345"
}
```

Copy `accessToken` from the response.

## 2. Register A New User

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "email": "you@example.com",
  "password": "Password123!"
}
```

## 3. Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "you@example.com",
  "password": "Password123!"
}
```

Copy `accessToken` from the response.

For all following requests, add:

```http
Authorization: Bearer <accessToken>
```

## 4. Create Notebook

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

Rename a notebook:

```http
PATCH /api/v1/notebooks/{notebookId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "title": "Updated notebook title"
}
```

## 5. Upload PDF

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

Add a website source:

```http
POST /api/v1/documents/{notebookId}/web-url
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "url": "https://example.com/article"
}
```

Add a YouTube transcript source:

```http
POST /api/v1/documents/{notebookId}/youtube
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "url": "https://www.youtube.com/watch?v=VIDEO_ID"
}
```

YouTube ingestion requires an available English transcript.

For reliable demos, paste the transcript manually:

```http
POST /api/v1/documents/{notebookId}/youtube-transcript
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "url": "https://www.youtube.com/watch?v=VIDEO_ID",
  "title": "Optional title",
  "transcript": "Paste transcript text here..."
}
```

## 6. Check Embedding Count

```http
GET /api/v1/embeddings/count
Authorization: Bearer <accessToken>
```

The count should increase after a successful upload.

## 7. Semantic Search

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

## 8. Ask Question

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

## 9. Persistent Chat

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

Clear saved messages:

```http
DELETE /api/v1/chat/notebooks/{notebookId}/messages
Authorization: Bearer <accessToken>
```

## 10. Studio Artifacts

Generate a Studio artifact:

```http
POST /api/v1/studio/notebooks/{notebookId}/artifacts
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "type": "FLASHCARDS",
  "instruction": "Focus on the most interview-relevant ideas"
}
```

Supported types:

```text
FLASHCARDS
QUIZ
BRIEFING
PODCAST_SCRIPT
INFOGRAPHIC_OUTLINE
```

List saved artifacts:

```http
GET /api/v1/studio/notebooks/{notebookId}/artifacts
Authorization: Bearer <accessToken>
```

Download generated files:

```http
GET /api/v1/studio/artifacts/{artifactId}/download?format=audio
GET /api/v1/studio/artifacts/{artifactId}/download?format=png
GET /api/v1/studio/artifacts/{artifactId}/download?format=jpg
```

Stream podcast audio:

```http
GET /api/v1/studio/artifacts/{artifactId}/audio
Authorization: Bearer <accessToken>
```

Stream infographic image:

```http
GET /api/v1/studio/artifacts/{artifactId}/image
Authorization: Bearer <accessToken>
```

Delete:

```http
DELETE /api/v1/studio/artifacts/{artifactId}
Authorization: Bearer <accessToken>
```

Studio stores Markdown and JSON for every artifact internally. Flashcards, quizzes, and briefings are used in-app. Podcast artifacts try to save a playable WAV file with Gemini TTS using configured Host A / Host B voices. Infographic artifacts save a generated PNG on the backend and can be downloaded as PNG or JPG.

Podcast voice defaults can be changed with:

```text
DOCMIND_STUDIO_TTS_HOST_A_VOICE
DOCMIND_STUDIO_TTS_HOST_B_VOICE
```

## 11. Settings

Profile:

```http
GET /api/v1/users/me
PATCH /api/v1/users/me
PUT /api/v1/users/me/password
DELETE /api/v1/users/me
```

Documents:

```http
GET /api/v1/documents
GET /api/v1/documents/notebooks/{notebookId}
DELETE /api/v1/documents/{documentId}
```
