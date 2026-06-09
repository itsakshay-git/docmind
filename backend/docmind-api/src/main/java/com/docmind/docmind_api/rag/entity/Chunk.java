package com.docmind.docmind_api.rag.entity;

import com.docmind.docmind_api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "chunks")
@Getter @Setter
public class Chunk extends BaseEntity {

    private UUID documentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer chunkIndex;
}
