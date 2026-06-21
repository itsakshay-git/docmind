package com.docmind.docmind_api.studio.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileSystemStudioMediaStorage implements StudioMediaStorage {

    private final String audioStorageDir;
    private final String imageStorageDir;

    public FileSystemStudioMediaStorage(
            String audioStorageDir,
            String imageStorageDir
    ) {
        this.audioStorageDir =
                audioStorageDir;
        this.imageStorageDir =
                imageStorageDir;
    }

    @Override
    public String saveAudio(
            UUID notebookId,
            byte[] bytes,
            String extension
    ) {

        return save(
                audioStorageDir,
                notebookId,
                bytes,
                extension
        );
    }

    @Override
    public String saveImage(
            UUID notebookId,
            byte[] bytes,
            String extension
    ) {

        return save(
                imageStorageDir,
                notebookId,
                bytes,
                extension
        );
    }

    @Override
    public byte[] read(
            String storageKey
    ) {

        try {
            return Files.readAllBytes(
                    Path.of(
                            storageKey
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read Studio media from filesystem path '%s'".formatted(storageKey),
                    e
            );
        }
    }

    @Override
    public void delete(
            String storageKey
    ) {

        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(
                    Path.of(
                            storageKey
                    )
            );
        } catch (Exception ignored) {
        }
    }

    private String save(
            String directory,
            UUID notebookId,
            byte[] bytes,
            String extension
    ) {

        try {
            Files.createDirectories(
                    Paths.get(
                            directory
                    )
            );

            Path path =
                    Paths.get(
                            directory,
                            notebookId
                                    + "-"
                                    + UUID.randomUUID()
                                    + "."
                                    + extension
                    );

            Files.write(
                    path,
                    bytes
            );

            return path.toString();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save Studio media to filesystem directory '%s'".formatted(directory),
                    e
            );
        }
    }
}