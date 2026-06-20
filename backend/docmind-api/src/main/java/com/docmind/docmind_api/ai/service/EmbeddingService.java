package com.docmind.docmind_api.ai.service;

import com.docmind.docmind_api.common.metrics.AiOperationMetrics;
import com.docmind.docmind_api.rag.entity.Chunk;
import com.docmind.docmind_api.rag.repository.EmbeddingRepository;
import com.docmind.docmind_api.rag.repository.EmbeddingVectorRepository;
import com.docmind.docmind_api.rag.repository.EmbeddingVectorRepository.EmbeddingVectorWrite;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingRepository embeddingRepository;
    private final EmbeddingVectorRepository embeddingVectorRepository;
    private final ObjectMapper objectMapper;
    private final AiOperationMetrics aiOperationMetrics;

    public void generateAndSaveEmbeddings(
            List<Chunk> chunks
    ) {

        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        aiOperationMetrics.record(
                "embedding.generate",
                () -> generateAndSaveEmbeddingsObserved(
                        chunks
                )
        );
    }

    private void generateAndSaveEmbeddingsObserved(
            List<Chunk> chunks
    ) {

        try {

            List<String> texts =
                    chunks.stream()
                            .map(Chunk::getContent)
                            .toList();

            EmbeddingResponse response =
                    embeddingModel.embedForResponse(texts);

            if (response.getResults().size() != chunks.size()) {
                throw new IllegalStateException(
                        "Expected "
                                + chunks.size()
                                + " embeddings but received "
                                + response.getResults().size()
                );
            }

            List<EmbeddingVectorWrite> embeddings =
                    new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {

                Chunk chunk = chunks.get(i);

                float[] output =
                        response.getResults()
                                .get(i)
                                .getOutput();

                embeddings.add(
                        new EmbeddingVectorWrite(
                                chunk.getId(),
                                toJsonVector(
                                        output
                                ),
                                embeddingVectorRepository.toVectorLiteral(
                                        output
                                )
                        )
                );
            }

            embeddingVectorRepository.saveAll(
                    embeddings
            );

            aiOperationMetrics.recordItems(
                    "embedding.generate",
                    "chunks",
                    chunks.size()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate embeddings",
                    e
            );
        }
    }

    private String toJsonVector(
            float[] output
    ) throws Exception {

        List<Double> vector =
                new ArrayList<>(
                        output.length
                );

        for (float value : output) {
            vector.add(
                    (double) value
            );
        }

        return objectMapper.writeValueAsString(
                vector
        );
    }

    public long countEmbeddings() {
        return embeddingRepository.count();
    }
}
