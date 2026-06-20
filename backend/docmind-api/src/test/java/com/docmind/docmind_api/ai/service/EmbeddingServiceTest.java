package com.docmind.docmind_api.ai.service;

import com.docmind.docmind_api.common.metrics.AiOperationMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.docmind.docmind_api.rag.entity.Chunk;
import com.docmind.docmind_api.rag.entity.Embedding;
import com.docmind.docmind_api.rag.repository.EmbeddingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmbeddingServiceTest {

    private final AiOperationMetrics aiOperationMetrics =
            new AiOperationMetrics(
                    new SimpleMeterRegistry()
            );

    private final EmbeddingModel embeddingModel =
            mock(EmbeddingModel.class);

    private final EmbeddingRepository embeddingRepository =
            mock(EmbeddingRepository.class);

    private final EmbeddingService embeddingService =
            new EmbeddingService(
                    embeddingModel,
                    embeddingRepository,
                    new ObjectMapper(),
                    aiOperationMetrics
            );

    @Test
    void emptyChunkListSavesNothing() {
        embeddingService.generateAndSaveEmbeddings(
                List.of()
        );

        verify(embeddingModel, never())
                .embedForResponse(
                        List.of()
                );

        verify(embeddingRepository, never())
                .saveAll(
                        List.of()
                );
    }

    @Test
    void savesOneEmbeddingForEachChunkInOrder() {
        Chunk firstChunk =
                chunk(
                        "first content"
                );

        Chunk secondChunk =
                chunk(
                        "second content"
                );

        when(
                embeddingModel.embedForResponse(
                        List.of(
                                "first content",
                                "second content"
                        )
                )
        ).thenReturn(
                new EmbeddingResponse(
                        List.of(
                                new org.springframework.ai.embedding.Embedding(
                                        new float[]{1.0f, 2.0f},
                                        0
                                ),
                                new org.springframework.ai.embedding.Embedding(
                                        new float[]{3.0f, 4.0f},
                                        1
                                )
                        )
                )
        );

        embeddingService.generateAndSaveEmbeddings(
                List.of(
                        firstChunk,
                        secondChunk
                )
        );

        ArgumentCaptor<Iterable<Embedding>> captor =
                ArgumentCaptor.forClass(
                        Iterable.class
                );

        verify(embeddingRepository)
                .saveAll(
                        captor.capture()
                );

        List<Embedding> savedEmbeddings =
                asList(
                        captor.getValue()
                );

        assertThat(savedEmbeddings)
                .hasSize(2);

        assertThat(savedEmbeddings.get(0).getChunkId())
                .isEqualTo(
                        firstChunk.getId()
                );

        assertThat(savedEmbeddings.get(0).getVector())
                .isEqualTo("[1.0,2.0]");

        assertThat(savedEmbeddings.get(1).getChunkId())
                .isEqualTo(
                        secondChunk.getId()
                );

        assertThat(savedEmbeddings.get(1).getVector())
                .isEqualTo("[3.0,4.0]");
    }

    @Test
    void throwsWhenEmbeddingCountDoesNotMatchChunkCount() {
        when(
                embeddingModel.embedForResponse(
                        List.of(
                                "first content",
                                "second content"
                        )
                )
        ).thenReturn(
                new EmbeddingResponse(
                        List.of(
                                new org.springframework.ai.embedding.Embedding(
                                        new float[]{1.0f, 2.0f},
                                        0
                                )
                        )
                )
        );

        assertThatThrownBy(() -> embeddingService.generateAndSaveEmbeddings(
                List.of(
                        chunk(
                                "first content"
                        ),
                        chunk(
                                "second content"
                        )
                )
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        "Failed to generate embeddings"
                )
                .hasRootCauseInstanceOf(
                        IllegalStateException.class
                )
                .hasRootCauseMessage(
                        "Expected 2 embeddings but received 1"
                );

        verify(embeddingRepository, never())
                .saveAll(
                        org.mockito.ArgumentMatchers.any()
                );
    }

    private static Chunk chunk(String content) {
        Chunk chunk =
                new Chunk();

        chunk.setId(
                UUID.randomUUID()
        );

        chunk.setContent(
                content
        );

        return chunk;
    }

    private static List<Embedding> asList(
            Iterable<Embedding> embeddings
    ) {
        return (List<Embedding>) embeddings;
    }
}
