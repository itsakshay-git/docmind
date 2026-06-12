package com.docmind.docmind_api.chat.service;

import com.docmind.docmind_api.chat.dto.ChatExchangeResponse;
import com.docmind.docmind_api.chat.dto.ChatMessageRequest;
import com.docmind.docmind_api.chat.dto.ChatMessageResponse;
import com.docmind.docmind_api.chat.entity.ChatMessage;
import com.docmind.docmind_api.chat.entity.ChatMessageRole;
import com.docmind.docmind_api.chat.entity.ChatSession;
import com.docmind.docmind_api.chat.repository.ChatMessageRepository;
import com.docmind.docmind_api.chat.repository.ChatSessionRepository;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.RagAskRequest;
import com.docmind.docmind_api.rag.dto.RagAskResponse;
import com.docmind.docmind_api.rag.dto.RagSource;
import com.docmind.docmind_api.rag.service.RagAnswerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String DEFAULT_SESSION_TITLE = "Notebook chat";

    private final NotebookRepository notebookRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RagAnswerService ragAnswerService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(
            UUID notebookId,
            String ownerEmail
    ) {

        Optional<ChatSession> session =
                findSession(
                        notebookId,
                        ownerEmail
                );

        if (session.isEmpty()) {
            return List.of();
        }

        return chatMessageRepository
                .findBySessionIdOrderByMessageOrderAsc(
                        session.get()
                                .getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChatExchangeResponse sendMessage(
            UUID notebookId,
            ChatMessageRequest request,
            String ownerEmail
    ) {

        ChatSession session =
                getOrCreateSession(
                        notebookId,
                        ownerEmail
                );

        int nextOrder =
                chatMessageRepository.countBySessionId(
                        session.getId()
                );

        ChatMessage userMessage =
                saveMessage(
                        session.getId(),
                        ChatMessageRole.USER,
                        normalizeContent(
                                request.getContent()
                        ),
                        null,
                        nextOrder
                );

        RagAskRequest askRequest =
                new RagAskRequest();

        askRequest.setQuestion(
                userMessage.getContent()
        );

        askRequest.setTopK(
                request.getTopK()
        );

        RagAskResponse ragResponse =
                ragAnswerService.ask(
                        notebookId,
                        askRequest,
                        ownerEmail
                );

        ChatMessage assistantMessage =
                saveMessage(
                        session.getId(),
                        ChatMessageRole.ASSISTANT,
                        ragResponse.getAnswer(),
                        toSourcesJson(
                                ragResponse.getSources()
                        ),
                        nextOrder + 1
                );

        return new ChatExchangeResponse(
                toResponse(userMessage),
                toResponse(assistantMessage)
        );
    }

    private Optional<ChatSession> findSession(
            UUID notebookId,
            String ownerEmail
    ) {

        notebookRepository
                .findByIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseThrow();

        return chatSessionRepository.findByNotebookIdAndOwnerEmail(
                notebookId,
                ownerEmail
        );
    }

    private ChatSession getOrCreateSession(
            UUID notebookId,
            String ownerEmail
    ) {

        Notebook notebook =
                notebookRepository
                        .findByIdAndOwnerEmail(
                                notebookId,
                                ownerEmail
                        )
                        .orElseThrow();

        return chatSessionRepository
                .findByNotebookIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseGet(() -> createSession(
                        notebook,
                        ownerEmail
                ));
    }

    private ChatSession createSession(
            Notebook notebook,
            String ownerEmail
    ) {

        ChatSession session =
                new ChatSession();

        session.setNotebookId(
                notebook.getId()
        );

        session.setOwnerEmail(
                ownerEmail
        );

        session.setTitle(
                DEFAULT_SESSION_TITLE
        );

        return chatSessionRepository.save(
                session
        );
    }

    private ChatMessage saveMessage(
            UUID sessionId,
            ChatMessageRole role,
            String content,
            String sourcesJson,
            int messageOrder
    ) {

        ChatMessage message =
                new ChatMessage();

        message.setSessionId(
                sessionId
        );

        message.setRole(
                role
        );

        message.setContent(
                content
        );

        message.setSourcesJson(
                sourcesJson
        );

        message.setMessageOrder(
                messageOrder
        );

        return chatMessageRepository.save(
                message
        );
    }

    private ChatMessageResponse toResponse(
            ChatMessage message
    ) {

        return new ChatMessageResponse(
                message.getId().toString(),
                message.getRole(),
                message.getContent(),
                parseSources(
                        message.getSourcesJson()
                ),
                message.getCreatedAt()
        );
    }

    private String normalizeContent(
            String content
    ) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Message content is required"
            );
        }

        return content.trim();
    }

    private String toSourcesJson(
            List<RagSource> sources
    ) {

        try {
            return objectMapper.writeValueAsString(
                    sources
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to serialize chat message sources",
                    e
            );
        }
    }

    private List<RagSource> parseSources(
            String sourcesJson
    ) {

        if (sourcesJson == null || sourcesJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    sourcesJson,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse chat message sources",
                    e
            );
        }
    }
}
