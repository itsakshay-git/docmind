package com.docmind.docmind_api.studio.entity;

import com.docmind.docmind_api.common.entity.BaseEntity;
import com.docmind.docmind_api.studio.enums.StudioArtifactType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "studio_artifacts")
public class StudioArtifact extends BaseEntity {

    @Column(nullable = false)
    private UUID notebookId;

    @Column(nullable = false)
    private String ownerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudioArtifactType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String markdownContent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String jsonContent;

    @Column(columnDefinition = "TEXT")
    private String sourceChunkIds;

    private String audioFilePath;

    private String audioMimeType;

    private String imageFilePath;

    private String imageMimeType;
}
