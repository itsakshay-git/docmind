package com.docmind.docmind_api.rag.entity;

import com.docmind.docmind_api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "embeddings")
@Getter
@Setter
public class Embedding extends BaseEntity {

    @Column(name = "chunk_id", nullable = false)
    private UUID chunkId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String vector;
}
