package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.common.metrics.AiOperationMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.docmind.docmind_api.document.entity.Document;
import com.docmind.docmind_api.document.repository.DocumentRepository;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import com.docmind.docmind_api.rag.entity.Chunk;
import com.docmind.docmind_api.rag.entity.Embedding;
import com.docmind.docmind_api.rag.repository.ChunkRepository;
import com.docmind.docmind_api.rag.repository.EmbeddingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyList;

class RagSearchServiceTest {

    private final AiOperationMetrics aiOperationMetrics =
            new AiOperationMetrics(
                    new SimpleMeterRegistry()
            );

    private final NotebookRepository notebookRepository =
            mock(NotebookRepository.class);

    private final DocumentRepository documentRepository =
            mock(DocumentRepository.class);

    private final ChunkRepository chunkRepository =
            mock(ChunkRepository.class);

    private final EmbeddingRepository embeddingRepository =
            mock(EmbeddingRepository.class);

    private final EmbeddingModel embeddingModel =
            mock(EmbeddingModel.class);

    private final RagSearchService ragSearchService =
            new RagSearchService(
                    notebookRepository,
                    documentRepository,
                    chunkRepository,
                    embeddingRepository,
                    embeddingModel,
                    new ObjectMapper(),
                    aiOperationMetrics
            );

    @Test
    void returnsTopResultsSortedByScore() {
        UUID notebookId =
                UUID.randomUUID();

        Document document =
                document(
                        notebookId
                );

        Chunk relevantChunk =
                chunk(
                        document.getId(),
                        "relevant content"
                );

        Chunk lessRelevantChunk =
                chunk(
                        document.getId(),
                        "less relevant content"
                );

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
                documentRepository.findByNotebookId(
                        notebookId
                )
        ).thenReturn(
                List.of(
                        document
                )
        );

        when(
                chunkRepository.findByDocumentIdIn(
                        List.of(
                                document.getId()
                        )
                )
        ).thenReturn(
                List.of(
                        relevantChunk,
                        lessRelevantChunk
                )
        );

        when(
                embeddingRepository.findByChunkIdIn(
                        anyList()
                )
        ).thenReturn(
                List.of(
                        embedding(
                                relevantChunk.getId(),
                                "[1.0,0.0]"
                        ),
                        embedding(
                                lessRelevantChunk.getId(),
                                "[0.0,1.0]"
                        )
                )
        );

        when(
                embeddingModel.embed(
                        "needle"
                )
        ).thenReturn(
                new float[]{1.0f, 0.0f}
        );

        SemanticSearchRequest request =
                request(
                        "needle",
                        1
                );

        List<SemanticSearchResult> results =
                ragSearchService.search(
                        notebookId,
                        request,
                        "user@example.com"
                );

        assertThat(results)
                .hasSize(1);

        assertThat(results.get(0).getChunkId())
                .isEqualTo(
                        relevantChunk.getId().toString()
                );

        assertThat(results.get(0).getScore())
                .isEqualTo(1.0);
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

        verify(documentRepository, never())
                .findByNotebookId(
                        notebookId
                );
    }

    @Test
    void returnsEmptyListWhenNotebookHasNoDocuments() {
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
                documentRepository.findByNotebookId(
                        notebookId
                )
        ).thenReturn(
                List.of()
        );

        List<SemanticSearchResult> results =
                ragSearchService.search(
                        notebookId,
                        request(
                                "question",
                                5
                        ),
                        "user@example.com"
                );

        assertThat(results)
                .isEmpty();

        verify(chunkRepository, never())
                .findByDocumentIdIn(
                        List.of()
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

    private static Document document(
            UUID notebookId
    ) {

        Document document =
                new Document();

        document.setId(
                UUID.randomUUID()
        );

        document.setNotebookId(
                notebookId
        );

        return document;
    }

    private static Chunk chunk(
            UUID documentId,
            String content
    ) {

        Chunk chunk =
                new Chunk();

        chunk.setId(
                UUID.randomUUID()
        );

        chunk.setDocumentId(
                documentId
        );

        chunk.setContent(
                content
        );

        return chunk;
    }

    private static Embedding embedding(
            UUID chunkId,
            String vector
    ) {

        Embedding embedding =
                new Embedding();

        embedding.setChunkId(
                chunkId
        );

        embedding.setVector(
                vector
        );

        return embedding;
    }
}
