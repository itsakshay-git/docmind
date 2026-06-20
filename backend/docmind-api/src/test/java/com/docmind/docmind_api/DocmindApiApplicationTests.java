package com.docmind.docmind_api;

import com.docmind.docmind_api.auth.repository.UserRepository;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import com.docmind.docmind_api.rag.repository.EmbeddingRepository;
import com.docmind.docmind_api.rag.repository.EmbeddingVectorRepository;
import com.docmind.docmind_api.rag.repository.EmbeddingVectorRepository.EmbeddingVectorWrite;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@SpringBootTest
@Testcontainers
@AutoConfigureObservability
class DocmindApiApplicationTests {

    private static final DockerImageName PGVECTOR_IMAGE =
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres");

    static {
        TimeZone.setDefault(
                TimeZone.getTimeZone("UTC")
        );
    }

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(PGVECTOR_IMAGE);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
        registry.add(
                "spring.ai.google.genai.api-key",
                () -> "test-gemini-key"
        );
        registry.add(
                "spring.ai.google.genai.embedding.api-key",
                () -> "test-gemini-key"
        );
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmbeddingRepository embeddingRepository;

    @Autowired
    private EmbeddingVectorRepository embeddingVectorRepository;

    @Autowired
    private PrometheusMeterRegistry prometheusMeterRegistry;

    @Test
    void contextLoadsWithFlywayPgvectorAndPostgres() {

        Integer appliedMigrationCount =
                jdbcTemplate.queryForObject(
                        "select count(*) from flyway_schema_history where success = true",
                        Integer.class
                );

        String vectorType =
                jdbcTemplate.queryForObject(
                        "select to_regtype('vector')::text",
                        String.class
                );

        Assertions.assertNotNull(appliedMigrationCount);
        Assertions.assertTrue(
                appliedMigrationCount >= 13
        );
        Assertions.assertEquals(
                "vector",
                vectorType
        );
        Assertions.assertTrue(
                userRepository.existsByEmail("recruiter@docmind.dev")
        );

        Assertions.assertNotNull(
                prometheusMeterRegistry
        );
    }

    @Test
    void pgvectorSearchReturnsClosestOwnedChunk() {
        UUID notebookId =
                UUID.randomUUID();
        UUID otherNotebookId =
                UUID.randomUUID();
        UUID documentId =
                UUID.randomUUID();
        UUID otherDocumentId =
                UUID.randomUUID();
        UUID relevantChunkId =
                UUID.randomUUID();
        UUID lessRelevantChunkId =
                UUID.randomUUID();
        UUID otherOwnerChunkId =
                UUID.randomUUID();

        insertNotebook(
                notebookId,
                "user@example.com"
        );
        insertNotebook(
                otherNotebookId,
                "other@example.com"
        );
        insertDocument(
                documentId,
                notebookId
        );
        insertDocument(
                otherDocumentId,
                otherNotebookId
        );
        insertChunk(
                relevantChunkId,
                documentId,
                "owned relevant content",
                0
        );
        insertChunk(
                lessRelevantChunkId,
                documentId,
                "owned less relevant content",
                1
        );
        insertChunk(
                otherOwnerChunkId,
                otherDocumentId,
                "other owner content",
                0
        );

        embeddingVectorRepository.saveAll(
                List.of(
                        embedding(
                                relevantChunkId,
                                vector(1.0f, 0.0f)
                        ),
                        embedding(
                                lessRelevantChunkId,
                                vector(0.0f, 1.0f)
                        ),
                        embedding(
                                otherOwnerChunkId,
                                vector(1.0f, 0.0f)
                        )
                )
        );

        List<SemanticSearchResult> results =
                embeddingVectorRepository.searchSimilar(
                        notebookId,
                        "user@example.com",
                        embeddingVectorRepository.toVectorLiteral(
                                vector(1.0f, 0.0f)
                        ),
                        2
                );

        Assertions.assertEquals(
                2,
                results.size()
        );
        Assertions.assertEquals(
                relevantChunkId.toString(),
                results.get(0).getChunkId()
        );
        Assertions.assertTrue(
                results.stream()
                        .noneMatch(result -> result.getChunkId()
                                .equals(otherOwnerChunkId.toString()))
        );

        embeddingRepository.deleteByChunkIdIn(
                List.of(
                        relevantChunkId,
                        lessRelevantChunkId
                )
        );

        Long remainingOwnedEmbeddings =
                jdbcTemplate.queryForObject(
                        "select count(*) from embeddings where chunk_id in (?, ?)",
                        Long.class,
                        relevantChunkId,
                        lessRelevantChunkId
                );

        Assertions.assertEquals(
                0L,
                remainingOwnedEmbeddings
        );
    }

    private void insertNotebook(
            UUID notebookId,
            String ownerEmail
    ) {

        jdbcTemplate.update(
                """
                        INSERT INTO notebooks (
                            id,
                            title,
                            owner_email,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                notebookId,
                "Notebook",
                ownerEmail,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private void insertDocument(
            UUID documentId,
            UUID notebookId
    ) {

        jdbcTemplate.update(
                """
                        INSERT INTO documents (
                            id,
                            notebook_id,
                            file_name,
                            file_path,
                            content,
                            status,
                            source_type,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                documentId,
                notebookId,
                "source.txt",
                null,
                "content",
                "PROCESSED",
                "YOUTUBE_TRANSCRIPT",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private void insertChunk(
            UUID chunkId,
            UUID documentId,
            String content,
            int chunkIndex
    ) {

        jdbcTemplate.update(
                """
                        INSERT INTO chunks (
                            id,
                            document_id,
                            content,
                            chunk_index,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                chunkId,
                documentId,
                content,
                chunkIndex,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private EmbeddingVectorWrite embedding(
            UUID chunkId,
            float[] vector
    ) {

        String vectorLiteral =
                embeddingVectorRepository.toVectorLiteral(
                        vector
                );

        return new EmbeddingVectorWrite(
                chunkId,
                vectorLiteral,
                vectorLiteral
        );
    }

    private float[] vector(
            float first,
            float second
    ) {

        float[] vector =
                new float[3072];

        vector[0] = first;
        vector[1] = second;

        return vector;
    }
}
