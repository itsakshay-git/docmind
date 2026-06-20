package com.docmind.docmind_api.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagConversationMessage {

    private String role;

    private String content;
}