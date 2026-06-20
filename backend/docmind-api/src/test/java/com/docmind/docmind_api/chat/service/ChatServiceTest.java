package com.docmind.docmind_api.chat.service;

import com.docmind.docmind_api.chat.dto.ChatMessageRequest;
import com.docmind.docmind_api.chat.entity.ChatMessage;
import com.docmind.docmind_api.chat.entity.ChatMessageRole;
import com.docmind.docmind_api.chat.entity.ChatSession;
import com.docmind.docmind_api.chat.repository.ChatMessageRepository;
import com.docmind.docmind_api.chat.repository.ChatSessionRepository;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.RagAskRequest;
import com.docmind.docmind_api.rag.dto.RagAskResponse;
import com.docmind.docmind_api.rag.service.RagAnswerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private final NotebookRepository notebookRepository =
            mock(NotebookRepository.class);

    private final ChatSessionRepository chatSessionRepository =
            mock(ChatSessionRepository.class);

    private final ChatMessageRepository chatMessageRepository =
            mock(ChatMessageRepository.class);

    private final RagAnswerService ragAnswerService =
            mock(RagAnswerService.class);

    private final ChatService chatService =
            new ChatService(
                    notebookRepository,
                    chatSessionRepository,
                    chatMessageRepository,
                    ragAnswerService,
                    new ObjectMapper()
            );

    @Test
    void sendsOnlyRecentExistingMessagesAsConversationMemory() {
        UUID notebookId =
                UUID.randomUUID();

        UUID sessionId =
                UUID.randomUUID();

        Notebook notebook =
                new Notebook();

        notebook.setId(
                notebookId
        );

        ChatSession session =
                new ChatSession();

        session.setId(
                sessionId
        );

        session.setNotebookId(
                notebookId
        );

        session.setOwnerEmail(
                "user@example.com"
        );

        session.setTitle(
                "Notebook chat"
        );

        List<ChatMessage> existingMessages =
                new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            existingMessages.add(
                    message(
                            sessionId,
                            i % 2 == 0
                                    ? ChatMessageRole.USER
                                    : ChatMessageRole.ASSISTANT,
                            "message-" + i,
                            i
                    )
            );
        }

        when(
                notebookRepository.findByIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(
                        notebook
                )
        );

        when(
                chatSessionRepository.findByNotebookIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(
                        session
                )
        );

        when(
                chatMessageRepository.findBySessionIdOrderByMessageOrderAsc(
                        sessionId
                )
        ).thenReturn(
                existingMessages
        );

        when(
                chatMessageRepository.save(
                        any(ChatMessage.class)
                )
        ).thenAnswer(invocation -> {
            ChatMessage saved =
                    invocation.getArgument(0);

            saved.setId(
                    UUID.randomUUID()
            );

            saved.setCreatedAt(
                    LocalDateTime.now()
            );

            saved.setUpdatedAt(
                    LocalDateTime.now()
            );

            return saved;
        });

        when(
                ragAnswerService.ask(
                        any(UUID.class),
                        any(RagAskRequest.class),
                        any(String.class)
                )
        ).thenReturn(
                new RagAskResponse(
                        "Assistant answer",
                        List.of()
                )
        );

        ChatMessageRequest request =
                new ChatMessageRequest();

        request.setContent(
                "Explain the second one."
        );

        request.setTopK(
                5
        );

        chatService.sendMessage(
                notebookId,
                request,
                "user@example.com"
        );

        ArgumentCaptor<RagAskRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        RagAskRequest.class
                );

        verify(ragAnswerService)
                .ask(
                        any(UUID.class),
                        requestCaptor.capture(),
                        any(String.class)
                );

        RagAskRequest ragRequest =
                requestCaptor.getValue();

        assertThat(ragRequest.getQuestion())
                .isEqualTo(
                        "Explain the second one."
                );

        assertThat(ragRequest.getTopK())
                .isEqualTo(5);

        assertThat(ragRequest.getConversationMemory())
                .hasSize(8)
                .extracting("content")
                .containsExactly(
                        "message-2",
                        "message-3",
                        "message-4",
                        "message-5",
                        "message-6",
                        "message-7",
                        "message-8",
                        "message-9"
                );
    }

    private static ChatMessage message(
            UUID sessionId,
            ChatMessageRole role,
            String content,
            int order
    ) {

        ChatMessage message =
                new ChatMessage();

        message.setId(
                UUID.randomUUID()
        );

        message.setSessionId(
                sessionId
        );

        message.setRole(
                role
        );

        message.setContent(
                content
        );

        message.setMessageOrder(
                order
        );

        message.setCreatedAt(
                LocalDateTime.now()
        );

        message.setUpdatedAt(
                LocalDateTime.now()
        );

        return message;
    }
}