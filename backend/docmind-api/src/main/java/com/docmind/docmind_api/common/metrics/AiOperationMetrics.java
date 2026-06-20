package com.docmind.docmind_api.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AiOperationMetrics {

    private static final String DEFAULT_VARIANT = "default";

    private final MeterRegistry meterRegistry;

    public <T> T record(
            String operation,
            Supplier<T> supplier
    ) {

        return record(
                operation,
                DEFAULT_VARIANT,
                supplier
        );
    }

    public <T> T record(
            String operation,
            String variant,
            Supplier<T> supplier
    ) {

        Timer.Sample sample =
                Timer.start(
                        meterRegistry
                );

        try {
            T result =
                    supplier.get();

            stop(
                    sample,
                    operation,
                    variant,
                    "success"
            );

            return result;
        } catch (RuntimeException e) {
            stop(
                    sample,
                    operation,
                    variant,
                    "error"
            );

            recordError(
                    operation,
                    variant,
                    e
            );

            throw e;
        }
    }

    public void record(
            String operation,
            Runnable runnable
    ) {

        record(
                operation,
                () -> {
                    runnable.run();
                    return null;
                }
        );
    }

    public Flux<String> recordFlux(
            String operation,
            String variant,
            Flux<String> flux
    ) {

        Timer.Sample sample =
                Timer.start(
                        meterRegistry
                );

        AtomicBoolean stopped =
                new AtomicBoolean(
                        false
                );

        return flux
                .doOnComplete(() -> stopOnce(
                        sample,
                        stopped,
                        operation,
                        variant,
                        "success"
                ))
                .doOnError(error -> {
                    stopOnce(
                            sample,
                            stopped,
                            operation,
                            variant,
                            "error"
                    );

                    recordError(
                            operation,
                            variant,
                            error
                    );
                })
                .doOnCancel(() -> stopOnce(
                        sample,
                        stopped,
                        operation,
                        variant,
                        "cancelled"
                ));
    }

    public void recordItems(
            String operation,
            String item,
            long count
    ) {

        recordItems(
                operation,
                DEFAULT_VARIANT,
                item,
                count
        );
    }

    public void recordItems(
            String operation,
            String variant,
            String item,
            long count
    ) {

        DistributionSummary.builder(
                        "docmind.ai.operation.items"
                )
                .tags(
                        Tags.of(
                                "operation",
                                operation,
                                "variant",
                                normalizeVariant(
                                        variant
                                ),
                                "item",
                                item
                        )
                )
                .register(
                        meterRegistry
                )
                .record(
                        count
                );
    }

    private void stopOnce(
            Timer.Sample sample,
            AtomicBoolean stopped,
            String operation,
            String variant,
            String status
    ) {

        if (stopped.compareAndSet(
                false,
                true
        )) {
            stop(
                    sample,
                    operation,
                    variant,
                    status
            );
        }
    }

    private void stop(
            Timer.Sample sample,
            String operation,
            String variant,
            String status
    ) {

        sample.stop(
                Timer.builder(
                                "docmind.ai.operation.duration"
                        )
                        .tags(
                                Tags.of(
                                        "operation",
                                        operation,
                                        "variant",
                                        normalizeVariant(
                                                variant
                                        ),
                                        "status",
                                        status
                                )
                        )
                        .register(
                                meterRegistry
                        )
        );
    }

    private void recordError(
            String operation,
            String variant,
            Throwable error
    ) {

        Counter.builder(
                        "docmind.ai.operation.errors"
                )
                .tags(
                        Tags.of(
                                "operation",
                                operation,
                                "variant",
                                normalizeVariant(
                                        variant
                                ),
                                "exception",
                                error.getClass().getSimpleName()
                        )
                )
                .register(
                        meterRegistry
                )
                .increment();
    }

    private String normalizeVariant(
            String variant
    ) {

        if (variant == null || variant.isBlank()) {
            return DEFAULT_VARIANT;
        }

        return variant.toLowerCase();
    }
}
