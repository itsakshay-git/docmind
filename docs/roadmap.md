# DocMind Roadmap

## Done

- Spring Boot 3 backend with Java 21.
- PostgreSQL and Flyway migrations.
- JWT authentication.
- Notebook CRUD.
- PDF upload into notebook scope.
- PDF text extraction with PDFBox.
- Chunk generation.
- Gemini embedding generation.
- Embedding persistence as JSON text.
- Notebook-scoped semantic search.
- Gemini-grounded one-off RAG answers.
- React, TypeScript, Vite frontend.
- Dark-only NotebookLM-inspired workspace.
- Persistent notebook chat history.
- Basic settings page for profile, password, notebooks, and documents.
- Notebook library hover actions for deleting notebooks.
- Notebook title editing from library cards.
- Workspace source list with document deletion.
- Website URL source ingestion.
- YouTube transcript source ingestion.
- Manual pasted YouTube transcript ingestion for reliable demos.
- Studio mini apps: flashcards, quiz, briefing, podcast audio/script, and generated infographic images.
- Studio artifact preview, podcast audio download, infographic PNG/JPG download, playback, and delete.

## Current Milestone

Make the product feel like a real document chat workspace:

- Persist user and assistant messages.
- Render Markdown assistant responses.
- Keep the workspace to three main areas: sources, chat, studio.
- Keep advanced retrieval settings hidden from normal users.
- Keep frontend and backend documentation current.

## Next Milestones

### Studio V2

- Add PDF export for Studio artifacts.
- Improve infographic rendering templates and add richer downloadable study formats.

### Better Retrieval

- Move vector storage from JSON text to `pgvector`.
- Add similarity search in PostgreSQL instead of in-memory Java scanning.
- Keep notebook ownership checks on every retrieval path.

### Streaming Answers

- Add a streaming chat endpoint.
- Render assistant tokens progressively in React.
- Keep the persisted final message once streaming completes.

### Source Management

- Add Brave Search import using `BRAVE_SEARCH_API_KEY`.
- Improve source details in the sidebar.
- Add source preview/snippets.
- Support re-indexing notebooks after source changes.

### Production Hardening

- Add integration tests with Testcontainers.
- Add rate-limit friendly AI retry handling.
- Add structured logging.
- Add CI build for backend and frontend.
