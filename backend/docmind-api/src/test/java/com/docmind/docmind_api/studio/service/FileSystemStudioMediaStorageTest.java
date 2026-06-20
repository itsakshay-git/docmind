package com.docmind.docmind_api.studio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemStudioMediaStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void savesReadsAndDeletesStudioMedia() {
        Path audioDir =
                tempDir.resolve(
                        "audio"
                );

        Path imageDir =
                tempDir.resolve(
                        "images"
                );

        FileSystemStudioMediaStorage storage =
                new FileSystemStudioMediaStorage(
                        audioDir.toString(),
                        imageDir.toString()
                );

        byte[] bytes =
                new byte[]{4, 5, 6};

        String storageKey =
                storage.saveImage(
                        UUID.randomUUID(),
                        bytes,
                        "png"
                );

        assertThat(Path.of(storageKey))
                .startsWith(imageDir);

        assertThat(storage.read(storageKey))
                .containsExactly(bytes);

        storage.delete(
                storageKey
        );

        assertThat(Files.exists(
                Path.of(storageKey)
        ))
                .isFalse();
    }
}