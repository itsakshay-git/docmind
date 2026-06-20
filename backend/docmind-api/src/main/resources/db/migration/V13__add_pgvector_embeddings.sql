CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE embeddings
    ADD COLUMN vector_embedding vector(3072);

UPDATE embeddings
SET vector_embedding = "vector"::vector(3072)
WHERE vector_embedding IS NULL;

ALTER TABLE embeddings
    ALTER COLUMN vector_embedding SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_documents_notebook_id
    ON documents (notebook_id);

CREATE INDEX IF NOT EXISTS idx_chunks_document_id
    ON chunks (document_id);

CREATE INDEX IF NOT EXISTS idx_embeddings_chunk_id
    ON embeddings (chunk_id);
