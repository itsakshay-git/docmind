package com.docmind.docmind_api.studio.dto;

public record StudioStorageSmokeTestResponse(
        String provider,
        boolean writeSucceeded,
        boolean readSucceeded,
        boolean deleteAttempted,
        boolean deleteSucceeded,
        boolean fallbackUsed,
        String storageKeyPrefix,
        String message,
        String rootCause
) {
}
