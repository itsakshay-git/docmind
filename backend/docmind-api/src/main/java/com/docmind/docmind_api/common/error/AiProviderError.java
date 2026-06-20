package com.docmind.docmind_api.common.error;

import org.springframework.http.HttpStatus;

public record AiProviderError(
        boolean providerError,
        HttpStatus status,
        String userMessage
) {

    public static AiProviderError none() {
        return new AiProviderError(
                false,
                HttpStatus.BAD_REQUEST,
                null
        );
    }
}