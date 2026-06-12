# DocMind ChatGPT Project Handoff

Source: `C:\Users\Akshay\Downloads\DocMind Project Handoff Summary.pdf`

## Product Goal

DocMind is a Retrieval-Augmented Generation (RAG) application that lets users upload documents, currently PDFs, process them into searchable chunks, generate AI embeddings, and ask natural-language questions grounded in the uploaded content.

Long-term workflow:

```text
User uploads PDF
-> Extract text
-> Chunk text
-> Generate embeddings
-> Store chunks and embeddings
-> User asks question
-> Semantic search finds relevant chunks
-> Gemini receives context and question
-> Grounded answer returned
```

Primary objective: build a production-ready document intelligence platform using Spring Boot, PostgreSQL, and Gemini.

## Tech Stack

- Java 21
- Spring Boot 3.5.14
- Spring Security
- Spring Data JPA
- Spring Validation
- Spring AI 1.1.7
- Flyway
- PostgreSQL
- Google Gemini
- Spring AI `ChatModel`
- Spring AI `EmbeddingModel`
- Swagger / OpenAPI
- SpringDoc
- Apache PDFBox 3.0.5
- JWT with `jjwt` 0.12.6
- Maven

## Completed Work

Authentication:

- User registration
- User login
- JWT generation
- JWT validation
- Security filter chain
- Protected endpoints

Database:

- PostgreSQL configured
- Flyway migrations
- JPA entities
- Repositories

PDF processing:

- PDF upload endpoint
- PDF storage metadata
- PDF text extraction using PDFBox

Chunking:

- Chunk entity
- Chunk repository
- Text chunk creation
- Chunk persistence

AI chat:

- Gemini `ChatModel` integration
- Successful API connection
- Chat test endpoint

AI embeddings:

- Gemini `EmbeddingModel` integration
- Embedding API connection
- Embedding test endpoint
- Verified `embeddingModel.embed("Hello World")`
- Result: vector size `3072`

Git:

- GitHub repository exists
- Push protection issue resolved
- API key removed from commits

## Database Model

Users:

- Purpose: authentication and ownership
- Fields roughly include `id`, `email`, `password`, `created_at`, `updated_at`

Documents:

- Purpose: uploaded PDF metadata
- Expected fields include `id`, `user_id`, `file_name`, `original_file_name`, `created_at`, `updated_at`

Chunks:

- Purpose: document fragments
- Fields include `id`, `document_id`, `content`, `chunk_index`, `created_at`, `updated_at`
- Relationship: document has many chunks

Embeddings:

- Current implementation stores `chunk_id` and `vector`
- `vector` is currently stored as `TEXT`
- Relationship target: chunk has one embedding

## Important Decisions

Gemini is the selected AI provider.

- Reason: free tier availability, native Spring AI support, and embedding support

Vectors are stored as `TEXT` during the MVP.

- Reason: simple implementation and faster progress
- Future: move to `pgvector`

Spring AI uses:

- `spring-ai-starter-model-google-genai`
- `spring-ai-starter-model-google-genai-embedding`
- Version `1.1.7`

Environment variables:

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
```

Do not commit API keys. Store the Gemini API key in the local run configuration or environment.

## Issues Encountered

Missing `EmbeddingModel` bean:

- Cause: embedding starter dependency was missing
- Fix: add `spring-ai-starter-model-google-genai-embedding`

Google GenAI project ID error:

- Cause: incorrect embedding configuration
- Fix: install official embedding starter and use proper Spring AI configuration

Invalid Gemini API key:

- Cause: environment variable was not loaded
- Fix: verify environment variable and regenerate key

GitHub push protection:

- Cause: API key was committed
- Fix: remove key, rewrite commit, use environment variable

## Technical Debt

Security:

- Verify whether `/embedding-test` and `/chat-test` should be exposed
- Previous work saw `403` errors, so security config should be reviewed

Embedding storage:

- Current storage is `TEXT`
- This is not suitable for production semantic search
- Future storage should use `pgvector`

Chunk-to-embedding relationship:

- Current approach uses a UUID reference
- Future approach should use a JPA relationship, such as `@OneToOne private Chunk chunk`

Embedding service duplication:

- There may have been duplicate services under `ai.service.EmbeddingService` and `ai.embedding.EmbeddingService`
- Consolidate into one implementation

## Immediate Next Steps

1. Create or finalize `EmbeddingService`.
2. Integrate embedding generation into `DocumentService`.
3. Save an embedding for each saved chunk.
4. Add or verify an endpoint/count check such as `GET /embeddings/count`.
5. Confirm chunks count equals embeddings count.
6. Implement query embeddings.
7. Implement semantic retrieval.
8. Build the first RAG pipeline:

```text
Question
-> Question embedding
-> Similarity search
-> Top chunks
-> Gemini prompt
-> Grounded answer
```

## Future Architecture

Phase 1:

- PDF to chunks to embeddings

Phase 2:

- Question to embedding to similarity search

Phase 3:

- RAG answer generation

Phase 4:

- Conversation memory

Phase 5:

- Multi-document retrieval

Phase 6:

- `pgvector` and production search

## Code Conventions

Use feature-based packages:

- `ai`
- `document`
- `rag`
- `security`
- `user`
- `common`

Entities:

- Use Lombok `@Getter` and `@Setter`
- Use JPA `@Entity`
- Extend `BaseEntity`

Services:

- Use `@Service`
- Use `@RequiredArgsConstructor`
- Use constructor injection only

Controllers:

- Use `@RestController`
- Use `@RequiredArgsConstructor`
- Do not use field injection

Database:

- All schema updates must go through Flyway migrations
- Add new migrations, for example `V7__add_xyz.sql`
- Never modify old migrations

