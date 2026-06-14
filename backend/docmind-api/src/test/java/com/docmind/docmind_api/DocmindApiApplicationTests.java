package com.docmind.docmind_api;

import com.docmind.docmind_api.auth.repository.UserRepository;
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

import java.util.TimeZone;

@SpringBootTest
@Testcontainers
@AutoConfigureObservability
class DocmindApiApplicationTests {

    static {
        TimeZone.setDefault(
                TimeZone.getTimeZone("UTC")
        );
    }

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

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
    private PrometheusMeterRegistry prometheusMeterRegistry;

    @Test
    void contextLoadsWithFlywayAndPostgres() {

        Integer appliedMigrationCount =
                jdbcTemplate.queryForObject(
                        "select count(*) from flyway_schema_history where success = true",
                        Integer.class
                );

        Assertions.assertNotNull(appliedMigrationCount);
        Assertions.assertTrue(
                appliedMigrationCount >= 12
        );
        Assertions.assertTrue(
                userRepository.existsByEmail("recruiter@docmind.dev")
        );

        Assertions.assertNotNull(
                prometheusMeterRegistry
        );
    }
}
