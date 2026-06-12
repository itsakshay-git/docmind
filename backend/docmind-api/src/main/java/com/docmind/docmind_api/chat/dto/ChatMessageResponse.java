package com.docmind.docmind_api.chat.dto;

import com.docmind.docmind_api.chat.entity.ChatMessageRole;
import com.docmind.docmind_api.rag.dto.RagSource;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatMessageResponse {

    private String id;

    private ChatMessageRole role;

    private String content;

    private List<RagSource> sources;

    private LocalDateTime createdAt;
}
