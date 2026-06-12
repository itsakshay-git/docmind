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
| file_name | VARCHAR | Uploaded file name |
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
| vector | TEXT | JSON vector for MVP |
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

## Future

Replace JSON vector storage with PostgreSQL `pgvector` when retrieval performance becomes the next milestone.
