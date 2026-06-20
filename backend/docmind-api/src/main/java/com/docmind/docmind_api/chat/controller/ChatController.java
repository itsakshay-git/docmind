package com.docmind.docmind_api.chat.controller;

import com.docmind.docmind_api.chat.dto.ChatExchangeResponse;
import com.docmind.docmind_api.chat.dto.ChatMessageRequest;
import com.docmind.docmind_api.chat.dto.ChatMessageResponse;
import com.docmind.docmind_api.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/notebooks/{notebookId}/messages")
    public List<ChatMessageResponse> getMessages(
            @PathVariable UUID notebookId,
            Authentication authentication
    ) {

        return chatService.getMessages(
                notebookId,
                authentication.getName()
        );
    }

    @PostMapping("/notebooks/{notebookId}/messages")
    public ChatExchangeResponse sendMessage(
            @PathVariable UUID notebookId,
            @RequestBody ChatMessageRequest request,
            Authentication authentication
    ) {

        return chatService.sendMessage(
                notebookId,
                request,
                authentication.getName()
        );
    }

    @PostMapping(
            value = "/notebooks/{notebookId}/messages/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamMessage(
            @PathVariable UUID notebookId,
            @RequestBody ChatMessageRequest request,
            Authentication authentication
    ) {

        return chatService.streamMessage(
                notebookId,
                request,
                authentication.getName()
        );
    }

    @DeleteMapping("/notebooks/{notebookId}/messages")
    public void clearMessages(
            @PathVariable UUID notebookId,
            Authentication authentication
    ) {

        chatService.clearMessages(
                notebookId,
                authentication.getName()
        );
    }
}