ALTER TABLE documents
    ADD COLUMN source_type VARCHAR(50) NOT NULL DEFAULT 'PDF';

ALTER TABLE documents
    ADD COLUMN source_url VARCHAR(2000);

ALTER TABLE documents
    ADD COLUMN failure_reason TEXT;

ALTER TABLE documents
    ALTER COLUMN file_path DROP NOT NULL;
