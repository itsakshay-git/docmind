package com.docmind.docmind_api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatExchangeResponse {

    private ChatMessageResponse userMessage;

    private ChatMessageResponse assistantMessage;
}
