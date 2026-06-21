package com.docmind.docmind_api.studio.service;

import com.docmind.docmind_api.studio.dto.StudioStorageSmokeTestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudioStorageSmokeTestService {

    private static final byte[] PAYLOAD =
            "docmind-studio-storage-smoke-test".getBytes(StandardCharsets.UTF_8);

    private final StudioMediaStorage studioMediaStorage;

    public StudioStorageSmokeTestResponse run() {

        String storageKey =
                null;

        try {
            storageKey =
                    studioMediaStorage.saveImage(
                            UUID.randomUUID(),
                            PAYLOAD,
                            "txt"
                    );

            byte[] readBytes =
                    studioMediaStorage.read(
                            storageKey
                    );

            boolean readSucceeded =
                    Arrays.equals(
                            PAYLOAD,
                            readBytes
                    );

            boolean deleteSucceeded =
                    deleteQuietly(
                            storageKey
                    );

            boolean fallbackUsed =
                    isFallbackKey(
                            storageKey
                    );

            return new StudioStorageSmokeTestResponse(
                    providerName(),
                    true,
                    readSucceeded,
                    true,
                    deleteSucceeded,
                    fallbackUsed,
                    storageKeyPrefix(storageKey),
                    messageFor(
                            readSucceeded,
                            deleteSucceeded,
                            fallbackUsed
                    ),
                    fallbackUsed
                            ? fallbackRootCause()
                            : null
            );
        } catch (Exception e) {
            boolean deleteAttempted =
                    storageKey != null;

            boolean deleteSucceeded =
                    deleteAttempted
                            && deleteQuietly(
                                    storageKey
                            );

            return new StudioStorageSmokeTestResponse(
                    providerName(),
                    storageKey != null,
                    false,
                    deleteAttempted,
                    deleteSucceeded,
                    isFallbackKey(storageKey),
                    storageKeyPrefix(storageKey),
                    "Studio storage smoke test failed.",
                    sanitizeRootCause(e)
            );
        }
    }

    private boolean deleteQuietly(
            String storageKey
    ) {

        try {
            studioMediaStorage.delete(
                    storageKey
            );

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String providerName() {

        return studioMediaStorage.getClass()
                .getSimpleName();
    }

    private boolean isFallbackKey(
            String storageKey
    ) {

        return storageKey != null
                && storageKey.startsWith("local-fallback:");
    }

    private String fallbackRootCause() {

        if (studioMediaStorage instanceof R2StudioMediaStorage r2Storage
                && r2Storage.lastSaveFailureSummary() != null) {
            return r2Storage.lastSaveFailureSummary();
        }

        return "Primary storage write failed; adapter returned local fallback key.";
    }

    private String messageFor(
            boolean readSucceeded,
            boolean deleteSucceeded,
            boolean fallbackUsed
    ) {

        if (fallbackUsed) {
            return "Studio storage smoke test used local fallback instead of primary storage.";
        }

        return readSucceeded && deleteSucceeded
                ? "Studio storage smoke test passed."
                : "Studio storage smoke test completed with warnings.";
    }

    private String storageKeyPrefix(
            String storageKey
    ) {

        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }

        if (isFallbackKey(storageKey)) {
            return "local-fallback";
        }

        int slashIndex =
                storageKey.indexOf('/');

        if (slashIndex > 0) {
            return storageKey.substring(
                    0,
                    slashIndex
            );
        }

        int separatorIndex =
                storageKey.indexOf('\\');

        if (separatorIndex > 0) {
            return storageKey.substring(
                    0,
                    separatorIndex
            );
        }

        int colonIndex =
                storageKey.indexOf(':');

        if (colonIndex > 0) {
            return storageKey.substring(
                    0,
                    colonIndex
            );
        }

        return storageKey;
    }

    private String sanitizeRootCause(
            Exception exception
    ) {

        Throwable current =
                exception;

        while (current.getCause() != null) {
            current =
                    current.getCause();
        }

        String message =
                current.getMessage();

        if (message == null || message.isBlank()) {
            message = "No detail message";
        }

        return current.getClass().getSimpleName()
                + ": "
                + message.replaceAll("(?i)(secret|key|token|credential)[^\\s,;]*", "$1-redacted");
    }
}


