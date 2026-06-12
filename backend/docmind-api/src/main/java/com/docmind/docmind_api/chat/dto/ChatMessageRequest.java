package com.docmind.docmind_api.chat.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {

    private String content;

    private Integer topK;
}
