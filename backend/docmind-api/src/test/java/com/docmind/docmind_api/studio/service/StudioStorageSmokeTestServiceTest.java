package com.docmind.docmind_api.studio.service;

import com.docmind.docmind_api.studio.dto.StudioStorageSmokeTestResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudioStorageSmokeTestServiceTest {

    private static final byte[] PAYLOAD =
            "docmind-studio-storage-smoke-test".getBytes(StandardCharsets.UTF_8);

    private final StudioMediaStorage studioMediaStorage =
            mock(StudioMediaStorage.class);

    private final StudioStorageSmokeTestService service =
            new StudioStorageSmokeTestService(
                    studioMediaStorage
            );

    @Test
    void returnsSuccessWhenStorageWritesReadsAndDeletes() {
        when(
                studioMediaStorage.saveImage(
                        any(UUID.class),
                        any(byte[].class),
                        eq("txt")
                )
        ).thenReturn(
                "studio-images/smoke.txt"
        );

        when(
                studioMediaStorage.read(
                        "studio-images/smoke.txt"
                )
        ).thenReturn(
                PAYLOAD
        );

        StudioStorageSmokeTestResponse response =
                service.run();

        assertThat(response.provider())
                .startsWith("StudioMediaStorage$MockitoMock");
        assertThat(response.writeSucceeded())
                .isTrue();
        assertThat(response.readSucceeded())
                .isTrue();
        assertThat(response.deleteAttempted())
                .isTrue();
        assertThat(response.deleteSucceeded())
                .isTrue();
        assertThat(response.fallbackUsed())
                .isFalse();
        assertThat(response.storageKeyPrefix())
                .isEqualTo("studio-images");
        assertThat(response.message())
                .isEqualTo("Studio storage smoke test passed.");
        assertThat(response.rootCause())
                .isNull();

        verify(studioMediaStorage)
                .delete(
                        "studio-images/smoke.txt"
                );
    }

    @Test
    void reportsFallbackWhenPrimaryStorageFallsBackToLocalDisk() {
        when(
                studioMediaStorage.saveImage(
                        any(UUID.class),
                        any(byte[].class),
                        eq("txt")
                )
        ).thenReturn(
                "local-fallback:storage/studio-r2-fallback/studio-images/smoke.txt"
        );

        when(
                studioMediaStorage.read(
                        "local-fallback:storage/studio-r2-fallback/studio-images/smoke.txt"
                )
        ).thenReturn(
                PAYLOAD
        );

        StudioStorageSmokeTestResponse response =
                service.run();

        assertThat(response.writeSucceeded())
                .isTrue();
        assertThat(response.readSucceeded())
                .isTrue();
        assertThat(response.deleteAttempted())
                .isTrue();
        assertThat(response.deleteSucceeded())
                .isTrue();
        assertThat(response.fallbackUsed())
                .isTrue();
        assertThat(response.storageKeyPrefix())
                .isEqualTo("local-fallback");
        assertThat(response.message())
                .isEqualTo("Studio storage smoke test used local fallback instead of primary storage.");
        assertThat(response.rootCause())
                .contains("Primary storage write failed");

        verify(studioMediaStorage)
                .delete(
                        "local-fallback:storage/studio-r2-fallback/studio-images/smoke.txt"
                );
    }

    @Test
    void returnsFailureWhenWriteFailsWithoutDelete() {
        doThrow(
                new RuntimeException(
                        "write failed"
                )
        ).when(studioMediaStorage)
                .saveImage(
                        any(UUID.class),
                        any(byte[].class),
                        eq("txt")
                );

        StudioStorageSmokeTestResponse response =
                service.run();

        assertThat(response.writeSucceeded())
                .isFalse();
        assertThat(response.readSucceeded())
                .isFalse();
        assertThat(response.deleteAttempted())
                .isFalse();
        assertThat(response.deleteSucceeded())
                .isFalse();
        assertThat(response.fallbackUsed())
                .isFalse();
        assertThat(response.message())
                .isEqualTo("Studio storage smoke test failed.");
        assertThat(response.rootCause())
                .contains("RuntimeException")
                .contains("write failed");

        verify(studioMediaStorage, never())
                .delete(
                        any(String.class)
                );
    }

    @Test
    void attemptsDeleteWhenReadFailsAfterWrite() {
        when(
                studioMediaStorage.saveImage(
                        any(UUID.class),
                        any(byte[].class),
                        eq("txt")
                )
        ).thenReturn(
                "studio-images/smoke.txt"
        );

        doThrow(
                new RuntimeException(
                        "read failed"
                )
        ).when(studioMediaStorage)
                .read(
                        "studio-images/smoke.txt"
                );

        StudioStorageSmokeTestResponse response =
                service.run();

        assertThat(response.writeSucceeded())
                .isTrue();
        assertThat(response.readSucceeded())
                .isFalse();
        assertThat(response.deleteAttempted())
                .isTrue();
        assertThat(response.deleteSucceeded())
                .isTrue();
        assertThat(response.fallbackUsed())
                .isFalse();
        assertThat(response.storageKeyPrefix())
                .isEqualTo("studio-images");
        assertThat(response.rootCause())
                .contains("read failed");

        verify(studioMediaStorage)
                .delete(
                        "studio-images/smoke.txt"
                );
    }
}
