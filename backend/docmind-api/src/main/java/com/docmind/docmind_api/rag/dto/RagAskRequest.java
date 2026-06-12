package com.docmind.docmind_api.rag.dto;

import lombok.Data;

@Data
public class RagAskRequest {

    private String question;

    private Integer topK;
}
