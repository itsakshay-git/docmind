CREATE TABLE studio_artifacts
(
    id UUID PRIMARY KEY,

    notebook_id UUID NOT NULL,

    owner_email VARCHAR(255) NOT NULL,

    type VARCHAR(64) NOT NULL,

    title VARCHAR(255) NOT NULL,

    markdown_content TEXT NOT NULL,

    json_content TEXT NOT NULL,

    source_chunk_ids TEXT,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX ix_studio_artifacts_notebook_owner
    ON studio_artifacts (notebook_id, owner_email);
