package com.docmind.docmind_api.rag.repository;

import com.docmind.docmind_api.rag.entity.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface EmbeddingRepository
        extends JpaRepository<Embedding, UUID> {

    @Transactional
    void deleteByChunkIdIn(
            List<UUID> chunkIds
    );
}
