CREATE TABLE chunks
(
    id UUID PRIMARY KEY,

    document_id UUID NOT NULL,

    content TEXT NOT NULL,

    chunk_index INTEGER NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);