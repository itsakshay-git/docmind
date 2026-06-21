package com.docmind.docmind_api.studio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class R2StudioMediaStorage implements StudioMediaStorage {

    private static final Logger log =
            LoggerFactory.getLogger(R2StudioMediaStorage.class);

    private static final String AUDIO_PREFIX =
            "studio-audio";

    private static final String IMAGE_PREFIX =
            "studio-images";

    private static final String LOCAL_FALLBACK_PREFIX =
            "local-fallback:";

    private static final String LOCAL_FALLBACK_DIRECTORY =
            "storage/studio-r2-fallback";

    private final S3Client s3Client;
    private final String bucket;

    public R2StudioMediaStorage(
            S3Client s3Client,
            String bucket
    ) {
        this.s3Client =
                s3Client;
        this.bucket =
                bucket;
    }

    @Override
    public String saveAudio(
            UUID notebookId,
            byte[] bytes,
            String extension
    ) {

        return save(
                AUDIO_PREFIX,
                notebookId,
                bytes,
                extension,
                contentType(
                        "audio",
                        extension
                )
        );
    }

    @Override
    public String saveImage(
            UUID notebookId,
            byte[] bytes,
            String extension
    ) {

        return save(
                IMAGE_PREFIX,
                notebookId,
                bytes,
                extension,
                contentType(
                        "image",
                        extension
                )
        );
    }

    @Override
    public byte[] read(
            String storageKey
    ) {

        if (isLocalFallbackKey(storageKey)) {
            return readLocalFallback(
                    storageKey
            );
        }

        try {
            ResponseBytes<GetObjectResponse> response =
                    s3Client.getObjectAsBytes(
                            GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(storageKey)
                                    .build()
                    );

            return response.asByteArray();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read Studio media from R2 bucket '%s' key '%s'".formatted(
                            bucket,
                            storageKey
                    ),
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

        if (isLocalFallbackKey(storageKey)) {
            deleteLocalFallback(
                    storageKey
            );
            return;
        }

        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(storageKey)
                            .build()
            );
        } catch (Exception e) {
            log.warn(
                    "Failed to delete Studio media from R2 bucket {} key {}",
                    bucket,
                    storageKey,
                    e
            );
        }
    }

    private String save(
            String prefix,
            UUID notebookId,
            byte[] bytes,
            String extension,
            String contentType
    ) {

        String key =
                prefix
                        + "/"
                        + notebookId
                        + "-"
                        + UUID.randomUUID()
                        + "."
                        + extension;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(bytes)
            );

            return key;
        } catch (Exception e) {
            log.warn(
                    "Failed to save Studio media to R2 bucket {} key {}. Falling back to local filesystem storage for this artifact.",
                    bucket,
                    key,
                    e
            );

            return saveLocalFallback(
                    prefix,
                    notebookId,
                    bytes,
                    extension
            );
        }
    }

    private String saveLocalFallback(
            String prefix,
            UUID notebookId,
            byte[] bytes,
            String extension
    ) {

        try {
            Path directory =
                    Paths.get(
                            LOCAL_FALLBACK_DIRECTORY,
                            prefix
                    );

            Files.createDirectories(
                    directory
            );

            Path path =
                    directory.resolve(
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

            return LOCAL_FALLBACK_PREFIX
                    + path;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save Studio media to R2 or local fallback storage",
                    e
            );
        }
    }

    private byte[] readLocalFallback(
            String storageKey
    ) {

        try {
            return Files.readAllBytes(
                    Path.of(
                            stripLocalFallbackPrefix(
                                    storageKey
                            )
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read Studio media from local fallback storage",
                    e
            );
        }
    }

    private void deleteLocalFallback(
            String storageKey
    ) {

        try {
            Files.deleteIfExists(
                    Path.of(
                            stripLocalFallbackPrefix(
                                    storageKey
                            )
                    )
            );
        } catch (Exception e) {
            log.warn(
                    "Failed to delete Studio media from local fallback storage {}",
                    storageKey,
                    e
            );
        }
    }

    private boolean isLocalFallbackKey(
            String storageKey
    ) {

        return storageKey != null
                && storageKey.startsWith(LOCAL_FALLBACK_PREFIX);
    }

    private String stripLocalFallbackPrefix(
            String storageKey
    ) {

        return storageKey.substring(
                LOCAL_FALLBACK_PREFIX.length()
        );
    }

    private String contentType(
            String type,
            String extension
    ) {

        if (extension == null || extension.isBlank()) {
            return type + "/octet-stream";
        }

        String normalized =
                extension.toLowerCase();

        if ("jpg".equals(normalized)) {
            normalized = "jpeg";
        }

        if ("wav".equals(normalized)) {
            return "audio/wav";
        }

        return type
                + "/"
                + normalized;
    }
}
