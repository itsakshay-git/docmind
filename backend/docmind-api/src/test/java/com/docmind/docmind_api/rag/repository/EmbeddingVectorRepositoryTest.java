package com.docmind.docmind_api.rag.repository;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EmbeddingVectorRepositoryTest {

    private final JdbcTemplate jdbcTemplate =
            mock(JdbcTemplate.class);

    private final EmbeddingVectorRepository repository =
            new EmbeddingVectorRepository(
                    jdbcTemplate
            );

    @Test
    void formatsPgvectorLiteral() {
        String literal =
                repository.toVectorLiteral(
                        new float[]{
                                1.25f,
                                -2.5f,
                                0.0f
                        }
                );

        assertThat(literal)
                .isEqualTo("[1.25,-2.5,0.0]");
    }

    @Test
    void saveAllIgnoresEmptyInput() {
        repository.saveAll(
                List.of()
        );

        verify(jdbcTemplate, never())
                .batchUpdate(
                        anyString(),
                        anyList(),
                        anyInt(),
                        any()
                );
    }
}
