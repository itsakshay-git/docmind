package com.docmind.docmind_api.studio.service;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

public class R2StudioMediaStorage implements StudioMediaStorage {

    private static final String AUDIO_PREFIX =
            "studio-audio";

    private static final String IMAGE_PREFIX =
            "studio-images";

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
                    "Failed to read Studio media",
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
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(storageKey)
                            .build()
            );
        } catch (Exception ignored) {
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
            throw new RuntimeException(
                    "Failed to save Studio media",
                    e
            );
        }
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