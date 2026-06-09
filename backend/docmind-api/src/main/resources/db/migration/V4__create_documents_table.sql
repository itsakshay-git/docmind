CREATE TABLE documents
(
    id UUID PRIMARY KEY,

    notebook_id UUID NOT NULL,

    file_name VARCHAR(500) NOT NULL,

    file_path VARCHAR(1000) NOT NULL,

    content TEXT,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);