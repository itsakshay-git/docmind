CREATE TABLE notebooks
(
    id UUID PRIMARY KEY,

    title VARCHAR(255) NOT NULL,

    owner_email VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);