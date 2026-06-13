ALTER TABLE studio_artifacts
    ADD COLUMN audio_file_path VARCHAR(1024),
    ADD COLUMN audio_mime_type VARCHAR(128);
