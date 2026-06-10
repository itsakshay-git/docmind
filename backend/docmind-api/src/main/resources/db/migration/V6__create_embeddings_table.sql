CREATE TABLE embeddings
(
    id UUID PRIMARY KEY,

    chunk_id UUID NOT NULL,

    vector TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);