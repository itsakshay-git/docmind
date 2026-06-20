package com.docmind.docmind_api.common.error;

import org.springframework.http.HttpStatus;

import java.util.Locale;

public final class AiProviderErrorClassifier {

    private AiProviderErrorClassifier() {
    }

    public static AiProviderError classify(
            Throwable throwable
    ) {

        String message =
                flattenMessages(
                        throwable
                );

        if (message.isBlank()) {
            return AiProviderError.none();
        }

        if (containsAny(
                message,
                "quota",
                "rate limit",
                "ratelimit",
                "too many requests",
                "resource_exhausted",
                "limit exceeded",
                "429"
        )) {
            return new AiProviderError(
                    true,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Gemini quota or rate limit was reached. Please wait a moment and try again."
            );
        }

        if (containsAny(
                message,
                "timeout",
                "timed out",
                "temporarily unavailable",
                "service unavailable",
                "connection reset",
                "connection refused",
                "connect timed out",
                "unknownhost",
                "network",
                "503",
                "504"
        )) {
            return new AiProviderError(
                    true,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI provider is temporarily unavailable. Please try again in a moment."
            );
        }

        if (containsAny(
                message,
                "generate content",
                "failed to generate embeddings",
                "failed to generate podcast audio",
                "gemini",
                "google genai",
                "ai provider"
        )) {
            return new AiProviderError(
                    true,
                    HttpStatus.BAD_GATEWAY,
                    "The AI provider could not complete the request. Please try again."
            );
        }

        return AiProviderError.none();
    }

    private static String flattenMessages(
            Throwable throwable
    ) {

        StringBuilder builder =
                new StringBuilder();

        Throwable current =
                throwable;

        while (current != null) {
            if (current.getMessage() != null) {
                builder.append(
                        current.getMessage()
                ).append(' ');
            }

            current =
                    current.getCause();
        }

        return builder.toString()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private static boolean containsAny(
            String message,
            String... needles
    ) {

        for (String needle : needles) {
            if (message.contains(
                    needle
            )) {
                return true;
            }
        }

        return false;
    }
}