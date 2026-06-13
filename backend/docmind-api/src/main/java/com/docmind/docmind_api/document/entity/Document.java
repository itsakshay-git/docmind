package com.docmind.docmind_api.document.entity;

import com.docmind.docmind_api.common.entity.BaseEntity;
import com.docmind.docmind_api.document.enums.DocumentSourceType;
import com.docmind.docmind_api.document.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @Column(name = "notebook_id", nullable = false)
    private UUID notebookId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private DocumentSourceType sourceType;

    @Column(name = "source_url", length = 2000)
    private String sourceUrl;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;
}
