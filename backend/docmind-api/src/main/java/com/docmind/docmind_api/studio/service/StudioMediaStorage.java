package com.docmind.docmind_api.studio.service;

import java.util.UUID;

public interface StudioMediaStorage {

    String saveAudio(
            UUID notebookId,
            byte[] bytes,
            String extension
    );

    String saveImage(
            UUID notebookId,
            byte[] bytes,
            String extension
    );

    byte[] read(
            String storageKey
    );

    void delete(
            String storageKey
    );
}