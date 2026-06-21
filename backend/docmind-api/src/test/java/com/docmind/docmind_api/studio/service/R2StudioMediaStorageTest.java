package com.docmind.docmind_api.studio.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class R2StudioMediaStorageTest {

    private final S3Client s3Client =
            mock(S3Client.class);

    private final R2StudioMediaStorage storage =
            new R2StudioMediaStorage(
                    s3Client,
                    "docmind-studio"
            );

    @Test
    void savesAudioToR2AndReturnsObjectKey() {
        when(
                s3Client.putObject(
                        any(PutObjectRequest.class),
                        any(RequestBody.class)
                )
        ).thenReturn(
                PutObjectResponse.builder()
                        .build()
        );

        UUID notebookId =
                UUID.randomUUID();

        String key =
                storage.saveAudio(
                        notebookId,
                        new byte[]{1, 2, 3},
                        "wav"
                );

        assertThat(key)
                .startsWith("studio-audio/" + notebookId + "-")
                .endsWith(".wav");

        ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        PutObjectRequest.class
                );

        verify(s3Client)
                .putObject(
                        requestCaptor.capture(),
                        any(RequestBody.class)
                );

        assertThat(requestCaptor.getValue().bucket())
                .isEqualTo("docmind-studio");
        assertThat(requestCaptor.getValue().key())
                .isEqualTo(key);
        assertThat(requestCaptor.getValue().contentType())
                .isEqualTo("audio/wav");
    }

    @Test
    void savesImageToR2AndReturnsObjectKey() {
        when(
                s3Client.putObject(
                        any(PutObjectRequest.class),
                        any(RequestBody.class)
                )
        ).thenReturn(
                PutObjectResponse.builder()
                        .build()
        );

        UUID notebookId =
                UUID.randomUUID();

        String key =
                storage.saveImage(
                        notebookId,
                        new byte[]{4, 5, 6},
                        "png"
                );

        assertThat(key)
                .startsWith("studio-images/" + notebookId + "-")
                .endsWith(".png");
    }

    @Test
    void fallsBackToLocalStorageWhenR2SaveFails() {
        doThrow(
                new RuntimeException(
                        "r2 unavailable"
                )
        ).when(s3Client)
                .putObject(
                        any(PutObjectRequest.class),
                        any(RequestBody.class)
                );

        String key =
                storage.saveImage(
                        UUID.randomUUID(),
                        new byte[]{10, 11, 12},
                        "png"
                );

        assertThat(key)
                .startsWith("local-fallback:");
        assertThat(storage.read(key))
                .containsExactly(10, 11, 12);

        storage.delete(
                key
        );
    }

    @Test
    void readsBytesFromR2() {
        when(
                s3Client.getObjectAsBytes(
                        any(GetObjectRequest.class)
                )
        ).thenReturn(
                ResponseBytes.fromByteArray(
                        GetObjectResponse.builder()
                                .build(),
                        new byte[]{7, 8, 9}
                )
        );

        byte[] bytes =
                storage.read(
                        "studio-images/example.png"
                );

        assertThat(bytes)
                .containsExactly(7, 8, 9);

        ArgumentCaptor<GetObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        GetObjectRequest.class
                );

        verify(s3Client)
                .getObjectAsBytes(
                        requestCaptor.capture()
                );

        assertThat(requestCaptor.getValue().bucket())
                .isEqualTo("docmind-studio");
        assertThat(requestCaptor.getValue().key())
                .isEqualTo("studio-images/example.png");
    }

    @Test
    void deletesObjectFromR2() {
        when(
                s3Client.deleteObject(
                        any(DeleteObjectRequest.class)
                )
        ).thenReturn(
                DeleteObjectResponse.builder()
                        .build()
        );

        storage.delete(
                "studio-audio/example.wav"
        );

        ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        DeleteObjectRequest.class
                );

        verify(s3Client)
                .deleteObject(
                        requestCaptor.capture()
                );

        assertThat(requestCaptor.getValue().bucket())
                .isEqualTo("docmind-studio");
        assertThat(requestCaptor.getValue().key())
                .isEqualTo("studio-audio/example.wav");
    }
}
