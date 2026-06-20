package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.common.metrics.AiOperationMetrics;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import com.docmind.docmind_api.rag.repository.EmbeddingVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RagSearchService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;

    private final NotebookRepository notebookRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingVectorRepository embeddingVectorRepository;
    private final AiOperationMetrics aiOperationMetrics;

    public List<SemanticSearchResult> search(
            UUID notebookId,
            SemanticSearchRequest request,
            String ownerEmail
    ) {

        return aiOperationMetrics.record(
                "rag.search",
                () -> searchObserved(
                        notebookId,
                        request,
                        ownerEmail
                )
        );
    }

    private List<SemanticSearchResult> searchObserved(
            UUID notebookId,
            SemanticSearchRequest request,
            String ownerEmail
    ) {

        notebookRepository
                .findByIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseThrow();

        float[] questionVector =
                embeddingModel.embed(
                        request.getQuestion()
                );

        List<SemanticSearchResult> results =
                embeddingVectorRepository.searchSimilar(
                        notebookId,
                        ownerEmail,
                        embeddingVectorRepository.toVectorLiteral(
                                questionVector
                        ),
                        normalizeTopK(
                                request.getTopK()
                        )
                );

        recordResultCount(
                results.size()
        );

        return results;
    }

    private void recordResultCount(
            int resultCount
    ) {

        aiOperationMetrics.recordItems(
                "rag.search",
                "results",
                resultCount
        );
    }

    private int normalizeTopK(
            Integer topK
    ) {

        if (topK == null || topK <= 0) {
            return DEFAULT_TOP_K;
        }

        return Math.min(
                topK,
                MAX_TOP_K
        );
    }
}
