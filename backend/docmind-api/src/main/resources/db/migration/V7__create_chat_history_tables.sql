CREATE TABLE chat_sessions
(
    id UUID PRIMARY KEY,

    notebook_id UUID NOT NULL,

    owner_email VARCHAR(255) NOT NULL,

    title VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_messages
(
    id UUID PRIMARY KEY,

    session_id UUID NOT NULL,

    role VARCHAR(32) NOT NULL,

    content TEXT NOT NULL,

    sources_json TEXT,

    message_order INTEGER NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX ux_chat_sessions_notebook_owner
    ON chat_sessions (notebook_id, owner_email);

CREATE INDEX ix_chat_messages_session_order
    ON chat_messages (session_id, message_order);
