package com.docmind.docmind_api.rag.repository;

import com.docmind.docmind_api.rag.entity.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmbeddingRepository
        extends JpaRepository<Embedding, UUID> {
}
