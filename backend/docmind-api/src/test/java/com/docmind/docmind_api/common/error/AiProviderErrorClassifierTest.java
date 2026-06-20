package com.docmind.docmind_api.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AiProviderErrorClassifierTest {

    @Test
    void classifiesQuotaAndRateLimitErrorsAsTooManyRequests() {
        AiProviderError error =
                AiProviderErrorClassifier.classify(
                        new RuntimeException(
                                "Google Gemini RESOURCE_EXHAUSTED: quota exceeded"
                        )
                );

        assertThat(error.providerError())
                .isTrue();

        assertThat(error.status())
                .isEqualTo(
                        HttpStatus.TOO_MANY_REQUESTS
                );

        assertThat(error.userMessage())
                .contains(
                        "quota or rate limit"
                );
    }

    @Test
    void classifiesTransientProviderErrorsAsServiceUnavailable() {
        AiProviderError error =
                AiProviderErrorClassifier.classify(
                        new RuntimeException(
                                "Failed to call Gemini",
                                new RuntimeException(
                                        "connect timed out"
                                )
                        )
                );

        assertThat(error.providerError())
                .isTrue();

        assertThat(error.status())
                .isEqualTo(
                        HttpStatus.SERVICE_UNAVAILABLE
                );

        assertThat(error.userMessage())
                .contains(
                        "temporarily unavailable"
                );
    }

    @Test
    void classifiesGenericGeminiFailuresAsBadGateway() {
        AiProviderError error =
                AiProviderErrorClassifier.classify(
                        new RuntimeException(
                                "Failed to generate embeddings"
                        )
                );

        assertThat(error.providerError())
                .isTrue();

        assertThat(error.status())
                .isEqualTo(
                        HttpStatus.BAD_GATEWAY
                );

        assertThat(error.userMessage())
                .contains(
                        "could not complete"
                );
    }

    @Test
    void doesNotClassifyBusinessValidationAsProviderFailure() {
        AiProviderError error =
                AiProviderErrorClassifier.classify(
                        new RuntimeException(
                                "Email already exists"
                        )
                );

        assertThat(error.providerError())
                .isFalse();
    }
}