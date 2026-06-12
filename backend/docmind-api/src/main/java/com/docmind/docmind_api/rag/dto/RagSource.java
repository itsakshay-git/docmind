package com.docmind.docmind_api.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RagSource {

    private String chunkId;

    private String documentId;

    private Double score;
}
