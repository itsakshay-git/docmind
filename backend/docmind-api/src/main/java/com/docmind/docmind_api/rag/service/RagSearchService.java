package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.common.metrics.AiOperationMetrics;
import com.docmind.docmind_api.document.entity.Document;
import com.docmind.docmind_api.document.repository.DocumentRepository;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import com.docmind.docmind_api.rag.entity.Chunk;
import com.docmind.docmind_api.rag.entity.Embedding;
import com.docmind.docmind_api.rag.repository.ChunkRepository;
import com.docmind.docmind_api.rag.repository.EmbeddingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagSearchService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;

    private final NotebookRepository notebookRepository;
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final EmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;
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

        List<Document> documents =
                documentRepository.findByNotebookId(
                        notebookId
                );

        if (documents.isEmpty()) {
            recordResultCount(
                    0
            );
            return List.of();
        }

        List<UUID> documentIds =
                documents.stream()
                        .map(Document::getId)
                        .toList();

        List<Chunk> chunks =
                chunkRepository.findByDocumentIdIn(
                        documentIds
                );

        if (chunks.isEmpty()) {
            recordResultCount(
                    0
            );
            return List.of();
        }

        Map<UUID, Chunk> chunksById =
                chunks.stream()
                        .collect(
                                Collectors.toMap(
                                        Chunk::getId,
                                        Function.identity()
                                )
                        );

        List<Embedding> embeddings =
                embeddingRepository.findByChunkIdIn(
                        chunksById.keySet()
                                .stream()
                                .toList()
                );

        if (embeddings.isEmpty()) {
            recordResultCount(
                    0
            );
            return List.of();
        }

        float[] questionVector =
                embeddingModel.embed(
                        request.getQuestion()
                );

        int limit =
                normalizeTopK(
                        request.getTopK()
                );

        List<SemanticSearchResult> results =
                embeddings.stream()
                        .map(embedding -> toResult(
                                embedding,
                                chunksById,
                                questionVector
                        ))
                        .filter(result -> result != null)
                        .sorted(
                                Comparator.comparing(
                                                SemanticSearchResult::getScore
                                        )
                                        .reversed()
                        )
                        .limit(limit)
                        .toList();

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

    private SemanticSearchResult toResult(
            Embedding embedding,
            Map<UUID, Chunk> chunksById,
            float[] questionVector
    ) {

        Chunk chunk =
                chunksById.get(
                        embedding.getChunkId()
                );

        if (chunk == null) {
            return null;
        }

        List<Double> storedVector =
                parseVector(
                        embedding.getVector()
                );

        return new SemanticSearchResult(
                chunk.getId().toString(),
                chunk.getDocumentId().toString(),
                chunk.getContent(),
                cosineSimilarity(
                        questionVector,
                        storedVector
                )
        );
    }

    private List<Double> parseVector(
            String vector
    ) {

        try {
            return objectMapper.readValue(
                    vector,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse stored embedding vector",
                    e
            );
        }
    }

    private double cosineSimilarity(
            float[] questionVector,
            List<Double> storedVector
    ) {

        int dimensions =
                Math.min(
                        questionVector.length,
                        storedVector.size()
                );

        double dotProduct = 0.0;
        double questionMagnitude = 0.0;
        double storedMagnitude = 0.0;

        for (int i = 0; i < dimensions; i++) {
            double questionValue =
                    questionVector[i];

            double storedValue =
                    storedVector.get(i);

            dotProduct += questionValue * storedValue;
            questionMagnitude += questionValue * questionValue;
            storedMagnitude += storedValue * storedValue;
        }

        if (questionMagnitude == 0.0 || storedMagnitude == 0.0) {
            return 0.0;
        }

        return dotProduct
                / (
                Math.sqrt(questionMagnitude)
                        * Math.sqrt(storedMagnitude)
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
