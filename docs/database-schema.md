# Database Schema

Migrations live in:

```text
backend/docmind-api/src/main/resources/db/migration
```

Use new Flyway migrations only. Do not edit old migrations after they have run locally.

## users

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| email | VARCHAR | Unique login identity |
| password_hash | VARCHAR | BCrypt hash |
| full_name | VARCHAR | Display name |
| role | VARCHAR | User role |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

## notebooks

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| title | VARCHAR | Notebook title |
| owner_email | VARCHAR | Current ownership link |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

## documents

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| notebook_id | UUID | Parent notebook |
| file_name | VARCHAR | Display name or uploaded file name |
| file_path | VARCHAR | Stored file path, nullable for non-file sources |
| source_type | VARCHAR | `PDF`, `WEB_URL`, `YOUTUBE`, or `YOUTUBE_TRANSCRIPT` |
| source_url | VARCHAR | Original URL for web and YouTube sources |
| failure_reason | TEXT | Failure detail for failed ingestion |
| content | TEXT | Extracted source text |
| status | VARCHAR | Processing status |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

## chunks

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| document_id | UUID | Source document |
| content | TEXT | Chunk text |
| chunk_index | INTEGER | Document-local order |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

## embeddings

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| chunk_id | UUID | Embedded chunk |
| vector | TEXT | Legacy JSON vector retained for compatibility |
| vector_embedding | vector(3072) | PostgreSQL pgvector embedding used for DB-side exact cosine search |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

## chat_sessions

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| notebook_id | UUID | Notebook conversation scope |
| owner_email | VARCHAR | Owner guard |
| title | VARCHAR | Session title |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

There is currently one default chat session per notebook and owner.
These tables are the source of truth for full chat history. Bounded chat memory is derived from recent messages rather than replacing this history store.

## chat_messages

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| session_id | UUID | Parent chat session |
| role | VARCHAR | USER or ASSISTANT |
| content | TEXT | Message body |
| sources_json | TEXT | Serialized RAG citations |
| message_order | INTEGER | Session-local ordering |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

## studio_artifacts

| Column | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| notebook_id | UUID | Notebook scope |
| owner_email | VARCHAR | Owner guard |
| type | VARCHAR | `FLASHCARDS`, `QUIZ`, `BRIEFING`, `PODCAST_SCRIPT`, or `INFOGRAPHIC_OUTLINE` |
| title | VARCHAR | Generated artifact title |
| markdown_content | TEXT | Human preview/download content |
| json_content | TEXT | Structured machine-readable artifact data |
| source_chunk_ids | TEXT | JSON list of retrieved chunk ids used for generation |
| audio_file_path | VARCHAR | Saved podcast WAV path, nullable |
| audio_mime_type | VARCHAR | Audio content type, nullable |
| image_file_path | VARCHAR | Saved infographic PNG path, nullable |
| image_mime_type | VARCHAR | Image content type, nullable |
| created_at | TIMESTAMP | Audit |
| updated_at | TIMESTAMP | Audit |

## Future

Embeddings now include PostgreSQL `pgvector` storage through a new Flyway migration while retaining the legacy JSON text vector for compatibility and Hibernate validation. Retrieval uses a narrow custom JDBC repository so notebook-owner filtering stays tied to DocMind's existing `documents` and `chunks` ownership model.
Future retrieval work should evaluate ANN indexing, hybrid keyword/vector search, reranking, and re-indexing flows without editing old migrations.
Podcast audio is saved as a generated WAV file through `StudioMediaStorage` when Gemini TTS succeeds. Infographic PNG bytes are saved through `StudioMediaStorage` using `storage/studio-images/` by default and converted to JPG on download when requested.
Future Studio media storage should add a durable object-storage adapter while keeping authenticated preview/download APIs stable.
