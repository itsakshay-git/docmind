package com.docmind.docmind_api.document.dto;

import com.docmind.docmind_api.document.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DocumentResponse {

    private String id;

    private String notebookId;

    private String fileName;

    private DocumentStatus status;

    private LocalDateTime createdAt;
}
