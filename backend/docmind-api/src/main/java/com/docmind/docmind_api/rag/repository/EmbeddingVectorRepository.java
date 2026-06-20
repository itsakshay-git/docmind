package com.docmind.docmind_api.rag.repository;

import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmbeddingVectorRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void saveAll(
            List<EmbeddingVectorWrite> embeddings
    ) {

        if (embeddings == null || embeddings.isEmpty()) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO embeddings (
                            id,
                            chunk_id,
                            vector,
                            vector_embedding,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?::vector, ?, ?)
                        """,
                embeddings,
                embeddings.size(),
                (statement, embedding) -> {
                    statement.setObject(
                            1,
                            UUID.randomUUID()
                    );
                    statement.setObject(
                            2,
                            embedding.chunkId()
                    );
                    statement.setString(
                            3,
                            embedding.jsonVector()
                    );
                    statement.setString(
                            4,
                            embedding.vectorLiteral()
                    );
                    statement.setTimestamp(
                            5,
                            Timestamp.valueOf(
                                    now
                            )
                    );
                    statement.setTimestamp(
                            6,
                            Timestamp.valueOf(
                                    now
                            )
                    );
                }
        );
    }

    public List<SemanticSearchResult> searchSimilar(
            UUID notebookId,
            String ownerEmail,
            String queryVectorLiteral,
            int limit
    ) {

        return jdbcTemplate.query(
                """
                        SELECT
                            c.id::text AS chunk_id,
                            c.document_id::text AS document_id,
                            c.content AS content,
                            1 - (e.vector_embedding <=> ?::vector) AS score
                        FROM embeddings e
                        JOIN chunks c ON c.id = e.chunk_id
                        JOIN documents d ON d.id = c.document_id
                        JOIN notebooks n ON n.id = d.notebook_id
                        WHERE d.notebook_id = ?
                          AND n.owner_email = ?
                        ORDER BY e.vector_embedding <=> ?::vector
                        LIMIT ?
                        """,
                (rs, rowNum) -> new SemanticSearchResult(
                        rs.getString("chunk_id"),
                        rs.getString("document_id"),
                        rs.getString("content"),
                        rs.getDouble("score")
                ),
                queryVectorLiteral,
                notebookId,
                ownerEmail,
                queryVectorLiteral,
                limit
        );
    }

    public String toVectorLiteral(
            float[] vector
    ) {

        StringBuilder builder =
                new StringBuilder("[");

        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }

            builder.append(
                    Float.toString(
                            vector[i]
                    )
            );
        }

        return builder.append(']')
                .toString();
    }

    public record EmbeddingVectorWrite(
            UUID chunkId,
            String jsonVector,
            String vectorLiteral
    ) {
    }
}
