package com.docmind.docmind_api.rag.repository;

import com.docmind.docmind_api.rag.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChunkRepository
        extends JpaRepository<Chunk, UUID> {
}
