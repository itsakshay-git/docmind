package com.docmind.docmind_api.ai.service;

import com.docmind.docmind_api.rag.entity.Chunk;
import com.docmind.docmind_api.rag.entity.Embedding;
import com.docmind.docmind_api.rag.repository.EmbeddingRepository;
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
    private final ObjectMapper objectMapper;

    public void generateAndSaveEmbeddings(
            List<Chunk> chunks
    ) {

        if (chunks == null || chunks.isEmpty()) {
            return;
        }

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

            List<Embedding> embeddings =
                    new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {

                Chunk chunk = chunks.get(i);

                float[] output =
                        response.getResults()
                                .get(i)
                                .getOutput();

                List<Double> vector =
                        new ArrayList<>(
                                output.length
                        );

                for (float value : output) {
                    vector.add(
                            (double) value
                    );
                }

                Embedding embedding =
                        new Embedding();

                embedding.setChunkId(
                        chunk.getId()
                );

                embedding.setVector(
                        objectMapper.writeValueAsString(
                                vector
                        )
                );

                embeddings.add(
                        embedding
                );
            }

            embeddingRepository.saveAll(
                    embeddings
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate embeddings",
                    e
            );
        }
    }

    public long countEmbeddings() {
        return embeddingRepository.count();
    }
}
