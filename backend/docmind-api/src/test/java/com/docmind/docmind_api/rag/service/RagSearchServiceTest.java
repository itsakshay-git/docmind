package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.common.metrics.AiOperationMetrics;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import com.docmind.docmind_api.rag.repository.EmbeddingVectorRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagSearchServiceTest {

    private final AiOperationMetrics aiOperationMetrics =
            new AiOperationMetrics(
                    new SimpleMeterRegistry()
            );

    private final NotebookRepository notebookRepository =
            mock(NotebookRepository.class);

    private final EmbeddingModel embeddingModel =
            mock(EmbeddingModel.class);

    private final EmbeddingVectorRepository embeddingVectorRepository =
            mock(EmbeddingVectorRepository.class);

    private final RagSearchService ragSearchService =
            new RagSearchService(
                    notebookRepository,
                    embeddingModel,
                    embeddingVectorRepository,
                    aiOperationMetrics
            );

    @Test
    void returnsDbRankedResults() {
        UUID notebookId =
                UUID.randomUUID();

        when(
                notebookRepository.findByIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(
                        new Notebook()
                )
        );

        when(
                embeddingModel.embed(
                        "needle"
                )
        ).thenReturn(
                new float[]{1.0f, 0.0f}
        );

        when(
                embeddingVectorRepository.toVectorLiteral(
                        new float[]{1.0f, 0.0f}
                )
        ).thenReturn("[1.0,0.0]");

        when(
                embeddingVectorRepository.searchSimilar(
                        notebookId,
                        "user@example.com",
                        "[1.0,0.0]",
                        1
                )
        ).thenReturn(
                List.of(
                        new SemanticSearchResult(
                                "chunk-1",
                                "document-1",
                                "relevant content",
                                0.99
                        )
                )
        );

        List<SemanticSearchResult> results =
                ragSearchService.search(
                        notebookId,
                        request(
                                "needle",
                                1
                        ),
                        "user@example.com"
                );

        assertThat(results)
                .hasSize(1);

        assertThat(results.get(0).getChunkId())
                .isEqualTo("chunk-1");
    }

    @Test
    void throwsWhenNotebookDoesNotBelongToUser() {
        UUID notebookId =
                UUID.randomUUID();

        when(
                notebookRepository.findByIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() -> ragSearchService.search(
                notebookId,
                request(
                        "question",
                        5
                ),
                "user@example.com"
        ))
                .isInstanceOf(
                        RuntimeException.class
                );

        verify(embeddingModel, never())
                .embed(
                        "question"
                );

        verify(embeddingVectorRepository, never())
                .searchSimilar(
                        any(),
                        any(),
                        any(),
                        any(Integer.class)
                );
    }

    @Test
    void normalizesTopKAndReturnsEmptyListWhenNoVectorsMatch() {
        UUID notebookId =
                UUID.randomUUID();

        when(
                notebookRepository.findByIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(
                        new Notebook()
                )
        );

        when(
                embeddingModel.embed(
                        "question"
                )
        ).thenReturn(
                new float[]{0.5f, 0.5f}
        );

        when(
                embeddingVectorRepository.toVectorLiteral(
                        new float[]{0.5f, 0.5f}
                )
        ).thenReturn("[0.5,0.5]");

        when(
                embeddingVectorRepository.searchSimilar(
                        notebookId,
                        "user@example.com",
                        "[0.5,0.5]",
                        5
                )
        ).thenReturn(
                List.of()
        );

        List<SemanticSearchResult> results =
                ragSearchService.search(
                        notebookId,
                        request(
                                "question",
                                0
                        ),
                        "user@example.com"
                );

        assertThat(results)
                .isEmpty();

        verify(embeddingVectorRepository)
                .searchSimilar(
                        notebookId,
                        "user@example.com",
                        "[0.5,0.5]",
                        5
                );
    }

    private static SemanticSearchRequest request(
            String question,
            Integer topK
    ) {

        SemanticSearchRequest request =
                new SemanticSearchRequest();

        request.setQuestion(
                question
        );

        request.setTopK(
                topK
        );

        return request;
    }
}
