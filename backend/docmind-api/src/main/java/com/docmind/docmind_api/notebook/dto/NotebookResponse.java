package com.docmind.docmind_api.notebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotebookResponse {

    private String id;

    private String title;

    private LocalDateTime createdAt;

    private long sourceCount;
}
