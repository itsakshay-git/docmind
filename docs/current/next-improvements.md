# DocMind Next Improvements

This backlog captures the post-v1 improvements for DocMind. V1 is deployed and includes persisted notebook chat history, source ingestion, RAG chat, Studio artifacts, CI, Docker, and deployment docs. The next phase should improve conversational quality, retrieval scale, media durability, quota handling, and production observability.

## Current Baseline

- Chat history is persisted in DocMind-owned `chat_sessions` and `chat_messages` tables.
- The current notebook chat answer path uses the latest user question, retrieved source chunks, a bounded window of recent prior chat turns, and a streaming chat endpoint for progressive UI rendering.
- Embeddings are written to PostgreSQL `pgvector` as 3072-dimensional Gemini vectors, with the legacy JSON `TEXT` value retained for compatibility. Similarity search now runs in PostgreSQL as exact cosine search.
- Studio podcast audio and infographic images are saved through a `StudioMediaStorage` abstraction. Filesystem storage remains the local default, and Cloudflare R2 is enabled and smoke-tested for durable production storage.
- Actuator health, Prometheus-ready metrics, custom AI/RAG operation metrics, and user-safe AI provider error messages exist, but production dashboards, structured logs, retries, and provider-category dashboards are not yet built.

Spring AI distinguishes chat history from chat memory. DocMind keeps full chat history in its own Spring Data tables and derives bounded prompt memory from recent notebook-owned turns. Reference: https://docs.spring.io/spring-ai/reference/api/chat-memory.html

## P0 / P1: Conversational Chat

- Continue hardening bounded conversational memory with regression examples for follow-up prompts like "explain the second one".
- Keep notebook ownership boundaries strict: memory must come only from the current notebook chat session and authenticated owner.
- Preserve the existing full history APIs and database tables as the source of truth for display, audit, and deletion.
- Continue hardening the streaming chat endpoint:
  - Keep `POST /api/v1/chat/notebooks/{notebookId}/messages` for non-streaming clients.
  - Keep `POST /api/v1/chat/notebooks/{notebookId}/messages/stream` as the streaming path.
  - Return `text/event-stream`.
  - Emit events such as `userMessage`, `token`, `sources`, `assistantMessage`, `error`, and `done`.
  - Persist the final assistant message and source citations when streaming completes.
- Keep progressive React rendering and fallback behavior healthy as the chat flow evolves.

Spring AI `ChatClient` supports streaming responses through its fluent streaming API. Reference: https://docs.spring.io/spring-ai/reference/api/chatclient.html

## P1: Retrieval And Source Quality

- Keep the custom `pgvector` JDBC repository healthy; it preserves the existing `documents` and `chunks` ownership model instead of adopting Spring AI's default `vector_store` table.
- Keep every retrieval path notebook-owner scoped while using database-side exact cosine search.
- Add retrieval regression coverage as more source types and chunk metadata are added.
- Add chunk metadata needed for better citations, source previews, and re-indexing.
- Add source preview/snippets in the workspace so citations are inspectable.
- Add re-indexing flows for source updates or future chunking/model changes.
- Add conversation-aware retrieval:
  - Rewrite follow-up questions into standalone search queries using recent chat memory.
  - Retrieve from notebook chunks using the rewritten query.
  - Keep the final answer grounded only in retrieved notebook context.
- Later improvements can include hybrid keyword/vector search, reranking, and retrieval evaluation datasets.
- ANN indexing is intentionally deferred because DocMind currently uses Gemini's 3072-dimensional embeddings. Exact `pgvector` search is the right milestone until corpus size proves an index is needed.

Spring AI PGvector setup requires PostgreSQL vector-related extensions and can initialize/search a vector table, but DocMind should still control ownership filtering and migration policy. Reference: https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html

## P1 / P2: Production Hardening

- Continue hardening rate-limit and quota-friendly AI handling:
  - Add bounded retries only where safe, such as transient provider failures.
  - Avoid retry storms for quota exhaustion.
  - Show actionable frontend errors for chat, embedding, and Studio generation failures.
- Keep Cloudflare R2 Studio media durable in production:
  - Keep the current filesystem-backed `StudioMediaStorage` adapter for local development.
  - Keep Render R2 endpoint, bucket, and access keys documented and smoke-tested.
  - Store durable object keys in the existing Studio media path fields.
  - Keep authenticated download and preview endpoints as the stable frontend contract.
- Continue improving observability:
  - Keep custom Micrometer metrics for embedding generation, RAG search, chat answer generation, streaming completion, and Studio generation healthy.
  - Add structured request and AI-operation logs.
  - Add provider-category dashboards for Gemini quota/rate-limit failures and transient errors.
  - Add a dashboard/runbook for Render, Neon, Gemini, and frontend smoke checks.

## P2 / P3: Studio And Product Polish

- Add PDF export for Studio artifacts.
- Improve infographic templates and visual hierarchy.
- Add regenerate actions for failed podcast audio or image generation.
- Add richer downloadable study formats where they help demos and learning workflows.
- Add Brave Search import behind `BRAVE_SEARCH_API_KEY`.
- Improve source management with source detail views, failed-ingestion guidance, and clearer status messaging.


## Future Enterprise Architecture Track

DocMind should not add Kafka or Redis just to list them on a resume. They become valuable only when the product needs asynchronous processing, distributed caching, throttling, or real-time status updates. A future "DocMind Enterprise" track can introduce them around real production problems.

### Redis Use Cases

- Cache expensive embedding results for repeated chunks or re-indexing workflows.
- Cache short-lived chat/RAG responses only when the prompt, notebook revision, user, and retrieval inputs are identical.
- Add distributed rate limiting for Gemini chat, embedding, TTS, and image calls.
- Store short-lived workflow/session state if DocMind later runs multiple backend instances.
- Cache source-processing status for fast workspace updates without repeatedly querying heavier tables.

### Kafka Use Cases

- Publish a `DocumentUploaded` event after source upload or paste.
- Split ingestion into asynchronous stages: text extraction, chunking, embedding, indexing, and notification.
- Add retry/dead-letter handling for failed extraction, embedding, or indexing jobs.
- Emit progress events so the frontend can show durable processing status.
- Keep notebook ownership and idempotency keys in each event so duplicate processing does not create duplicate chunks or embeddings.

### Example Enterprise Flow

```text
PDF Uploaded
  -> Kafka DocumentUploaded event
  -> Text Extraction Service
  -> Chunking Service
  -> Embedding Service
  -> Indexing Service
  -> Notification / Status Service
```

### Resume Value

- Event-driven document processing with Spring Boot workers.
- Asynchronous ingestion that keeps upload requests fast.
- Redis-backed rate limiting and caching for AI cost control.
- Message-driven architecture with retries, dead-letter handling, and idempotent consumers.

This is a future senior-level upgrade path. It should stay out of the current portfolio MVP until the deployed v1 demo is stable, documented, and easy to explain.

## Acceptance Criteria For Future Work

- Follow-up chat uses recent prior turns without leaking unrelated notebooks or users.
- Streaming chat progressively renders tokens and saves one final assistant message with citations.
- `pgvector` search returns only chunks from notebooks owned by the authenticated user.
- Quota/rate-limit failures continue to produce helpful messages instead of raw provider stack details.
- Production pgvector rollout is verified by Neon `vector` extension checks, Render health, source ingestion, chat retrieval, follow-up chat, and one Studio artifact smoke test.
- Studio media remains available after backend restarts and redeploys through the Cloudflare R2 adapter.
- Docs, API contracts, and deployment notes are updated in the same PR as each feature.

