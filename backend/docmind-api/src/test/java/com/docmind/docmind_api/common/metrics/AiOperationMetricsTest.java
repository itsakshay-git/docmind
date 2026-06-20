package com.docmind.docmind_api.common.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiOperationMetricsTest {

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    private final AiOperationMetrics aiOperationMetrics =
            new AiOperationMetrics(
                    meterRegistry
            );

    @Test
    void recordsSuccessfulOperationDuration() {
        String result =
                aiOperationMetrics.record(
                        "rag.answer",
                        "non_streaming",
                        () -> "ok"
                );

        assertThat(result)
                .isEqualTo("ok");

        assertThat(meterRegistry.find("docmind.ai.operation.duration")
                .tag("operation", "rag.answer")
                .tag("variant", "non_streaming")
                .tag("status", "success")
                .timer())
                .isNotNull();
    }

    @Test
    void recordsFailedOperationDurationAndErrorCounter() {
        assertThatThrownBy(() -> aiOperationMetrics.record(
                "embedding.generate",
                () -> {
                    throw new IllegalStateException("boom");
                }
        ))
                .isInstanceOf(IllegalStateException.class);

        assertThat(meterRegistry.find("docmind.ai.operation.duration")
                .tag("operation", "embedding.generate")
                .tag("variant", "default")
                .tag("status", "error")
                .timer())
                .isNotNull();

        assertThat(meterRegistry.find("docmind.ai.operation.errors")
                .tag("operation", "embedding.generate")
                .tag("exception", "IllegalStateException")
                .counter()
                .count())
                .isEqualTo(1.0);
    }

    @Test
    void recordsFluxCompletionAndItemSummary() {
        aiOperationMetrics.recordFlux(
                        "rag.answer.stream",
                        "gemini",
                        Flux.just("a", "b")
                )
                .collectList()
                .block();

        aiOperationMetrics.recordItems(
                "rag.search",
                "results",
                3
        );

        assertThat(meterRegistry.find("docmind.ai.operation.duration")
                .tag("operation", "rag.answer.stream")
                .tag("variant", "gemini")
                .tag("status", "success")
                .timer())
                .isNotNull();

        assertThat(meterRegistry.find("docmind.ai.operation.items")
                .tag("operation", "rag.search")
                .tag("item", "results")
                .summary()
                .totalAmount())
                .isEqualTo(3.0);
    }
}
