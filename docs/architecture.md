# DocMind Architecture

DocMind is a Spring Boot and React document intelligence platform inspired by NotebookLM.

The current MVP is a modular monolith backend plus a Vite React frontend.

## Current Capabilities

- User registration and JWT login.
- Notebook creation and ownership checks.
- PDF upload into a notebook.
- PDF text extraction with PDFBox.
- Text chunking.
- Gemini embedding generation.
- Embedding storage as JSON text.
- Notebook-scoped semantic retrieval.
- Gemini-grounded answers.
- Persistent notebook chat history.
- React workspace with dark/light theme support, sources, chat, and studio areas.

## Backend

Backend path:

```text
backend/docmind-api/
```

Main modules:

- `auth`: users, login, registration.
- `notebook`: notebook CRUD and ownership.
- `document`: PDF, website, and YouTube transcript ingestion, parsing, and document records.
- `ai`: embedding pipeline support.
- `rag`: semantic search and grounded answer generation.
- `chat`: persisted notebook chat sessions and messages.
- `security`: JWT authentication.
- `common`: shared errors and base entities.

More detail:

```text
docs/backend-architecture.md
```

## Frontend

Frontend path:

```text
frontend/
```

Main areas:

- Notebook list page.
- Notebook workspace page.
- Sources/sidebar.
- Chat history panel.
- Studio panel for generated study artifacts.

More detail:

```text
docs/frontend-architecture.md
```

## Local Infrastructure

Local development uses Docker Compose for PostgreSQL only. The backend exposes a minimal Spring Boot Actuator baseline for health, info, and metrics.

More detail:

```text
docs/local-development.md
```

## Upload-To-Answer Flow

```text
Source upload
-> document metadata
-> text extraction
-> chunks
-> Gemini embeddings
-> JSON vector storage
-> notebook-scoped retrieval
-> Gemini answer
-> persisted chat messages
```

## Near-Term Direction

Studio now supports flashcards, quizzes, briefings, podcast audio/script, and infographic images.

The next infrastructure upgrade is moving embeddings from JSON text to PostgreSQL `pgvector`. Prometheus, Grafana, Kubernetes, and cloud deployment are later milestones.
