package com.docmind.docmind_api.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SemanticSearchResult {

    private String chunkId;

    private String documentId;

    private String content;

    private Double score;
}
