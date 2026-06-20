package com.docmind.docmind_api.chat.service;

import com.docmind.docmind_api.chat.dto.ChatExchangeResponse;
import com.docmind.docmind_api.chat.dto.ChatMessageRequest;
import com.docmind.docmind_api.chat.dto.ChatMessageResponse;
import com.docmind.docmind_api.chat.entity.ChatMessage;
import com.docmind.docmind_api.chat.entity.ChatMessageRole;
import com.docmind.docmind_api.chat.entity.ChatSession;
import com.docmind.docmind_api.chat.repository.ChatMessageRepository;
import com.docmind.docmind_api.chat.repository.ChatSessionRepository;
import com.docmind.docmind_api.common.error.AiProviderError;
import com.docmind.docmind_api.common.error.AiProviderErrorClassifier;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.RagAskRequest;
import com.docmind.docmind_api.rag.dto.RagAskResponse;
import com.docmind.docmind_api.rag.dto.RagConversationMessage;
import com.docmind.docmind_api.rag.dto.RagSource;
import com.docmind.docmind_api.rag.service.RagAnswerService;
import com.docmind.docmind_api.rag.service.RagAnswerStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String DEFAULT_SESSION_TITLE = "Notebook chat";
    private static final int MAX_MEMORY_MESSAGES = 8;
    private static final long STREAM_TIMEOUT_MILLIS = 120_000L;

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

        PreparedChatMessage prepared =
                prepareUserMessage(
                        notebookId,
                        request,
                        ownerEmail
                );

        RagAskResponse ragResponse =
                ragAnswerService.ask(
                        notebookId,
                        prepared.askRequest(),
                        ownerEmail
                );

        ChatMessage assistantMessage =
                saveMessage(
                        prepared.session().getId(),
                        ChatMessageRole.ASSISTANT,
                        ragResponse.getAnswer(),
                        toSourcesJson(
                                ragResponse.getSources()
                        ),
                        prepared.nextOrder() + 1
                );

        return new ChatExchangeResponse(
                toResponse(prepared.userMessage()),
                toResponse(assistantMessage)
        );
    }

    public SseEmitter streamMessage(
            UUID notebookId,
            ChatMessageRequest request,
            String ownerEmail
    ) {

        SseEmitter emitter =
                new SseEmitter(
                        STREAM_TIMEOUT_MILLIS
                );

        CompletableFuture.runAsync(() -> streamMessage(
                notebookId,
                request,
                ownerEmail,
                emitter
        ));

        return emitter;
    }

    @Transactional
    public void clearMessages(
            UUID notebookId,
            String ownerEmail
    ) {

        Optional<ChatSession> session =
                findSession(
                        notebookId,
                        ownerEmail
                );

        session.ifPresent(chatSession -> chatMessageRepository.deleteBySessionId(
                chatSession.getId()
        ));
    }

    private void streamMessage(
            UUID notebookId,
            ChatMessageRequest request,
            String ownerEmail,
            SseEmitter emitter
    ) {

        try {
            PreparedChatMessage prepared =
                    prepareUserMessage(
                            notebookId,
                            request,
                            ownerEmail
                    );

            sendEvent(
                    emitter,
                    "userMessage",
                    toResponse(
                            prepared.userMessage()
                    )
            );

            RagAnswerStream stream =
                    ragAnswerService.stream(
                            notebookId,
                            prepared.askRequest(),
                            ownerEmail
                    );

            sendEvent(
                    emitter,
                    "sources",
                    stream.sources()
            );

            StringBuilder answer =
                    new StringBuilder();

            stream.tokens()
                    .filter(token -> token != null
                            && !token.isEmpty())
                    .doOnNext(token -> {
                        answer.append(
                                token
                        );

                        sendEvent(
                                emitter,
                                "token",
                                Map.of(
                                        "token",
                                        token
                                )
                        );
                    })
                    .blockLast();

            ChatMessage assistantMessage =
                    saveMessage(
                            prepared.session().getId(),
                            ChatMessageRole.ASSISTANT,
                            answer.toString(),
                            toSourcesJson(
                                    stream.sources()
                            ),
                            prepared.nextOrder() + 1
                    );

            sendEvent(
                    emitter,
                    "assistantMessage",
                    toResponse(
                            assistantMessage
                    )
            );

            sendEvent(
                    emitter,
                    "done",
                    "ok"
            );

            emitter.complete();
        } catch (Exception exception) {
            try {
                sendEvent(
                        emitter,
                        "error",
                        streamErrorMessage(
                                exception
                        )
                );
            } catch (Exception ignored) {
                // The connection may already be closed.
            }

            emitter.completeWithError(
                    exception
            );
        }
    }

    private PreparedChatMessage prepareUserMessage(
            UUID notebookId,
            ChatMessageRequest request,
            String ownerEmail
    ) {

        ChatSession session =
                getOrCreateSession(
                        notebookId,
                        ownerEmail
                );

        List<ChatMessage> existingMessages =
                chatMessageRepository.findBySessionIdOrderByMessageOrderAsc(
                        session.getId()
                );

        int nextOrder =
                existingMessages.size();

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

        askRequest.setConversationMemory(
                toConversationMemory(
                        existingMessages
                )
        );

        return new PreparedChatMessage(
                session,
                userMessage,
                askRequest,
                nextOrder
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

    private List<RagConversationMessage> toConversationMemory(
            List<ChatMessage> messages
    ) {

        if (messages.isEmpty()) {
            return List.of();
        }

        int start =
                Math.max(
                        messages.size() - MAX_MEMORY_MESSAGES,
                        0
                );

        return messages.subList(
                        start,
                        messages.size()
                )
                .stream()
                .map(message -> new RagConversationMessage(
                        message.getRole().name(),
                        message.getContent()
                ))
                .toList();
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

    private void sendEvent(
            SseEmitter emitter,
            String name,
            Object data
    ) {

        try {
            emitter.send(
                    SseEmitter.event()
                            .name(
                                    name
                            )
                            .data(
                                    data
                            )
            );
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Failed to stream chat response",
                    exception
            );
        }
    }

    private String streamErrorMessage(
            Exception exception
    ) {

        AiProviderError providerError =
                AiProviderErrorClassifier.classify(
                        exception
                );

        if (providerError.providerError()) {
            return providerError.userMessage();
        }

        return exception.getMessage() == null
                ? "Message failed. Please try again."
                : exception.getMessage();
    }
    private record PreparedChatMessage(
            ChatSession session,
            ChatMessage userMessage,
            RagAskRequest askRequest,
            int nextOrder
    ) {
    }
}