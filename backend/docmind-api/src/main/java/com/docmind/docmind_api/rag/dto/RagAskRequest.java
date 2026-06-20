package com.docmind.docmind_api.rag.dto;

import lombok.Data;

import java.util.List;

@Data
public class RagAskRequest {

    private String question;

    private Integer topK;

    private List<RagConversationMessage> conversationMemory;
}