ALTER TABLE studio_artifacts
    ADD COLUMN image_file_path VARCHAR(1024),
    ADD COLUMN image_mime_type VARCHAR(128);
