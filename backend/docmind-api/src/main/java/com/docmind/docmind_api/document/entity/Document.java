package com.docmind.docmind_api.document.entity;

import com.docmind.docmind_api.common.entity.BaseEntity;
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

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;
}
