package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.rag.dto.RagAskRequest;
import com.docmind.docmind_api.rag.dto.RagAskResponse;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagAnswerServiceTest {

    private final RagSearchService ragSearchService =
            mock(RagSearchService.class);

    private final ChatModel chatModel =
            mock(ChatModel.class);

    private final RagAnswerService ragAnswerService =
            new RagAnswerService(
                    ragSearchService,
                    chatModel
            );

    @Test
    void answersUsingRetrievedChunksAndReturnsSources() {
        UUID notebookId =
                UUID.randomUUID();

        RagAskRequest request =
                request(
                        "What are Java variable naming rules?",
                        5
                );

        when(
                ragSearchService.search(
                        any(UUID.class),
                        any(SemanticSearchRequest.class),
                        any(String.class)
                )
        ).thenReturn(
                List.of(
                        new SemanticSearchResult(
                                "chunk-1",
                                "document-1",
                                "Variable names can contain letters and digits.",
                                0.91
                        )
                )
        );

        when(
                chatModel.call(
                        any(String.class)
                )
        ).thenReturn(
                "Java variable names can contain letters and digits."
        );

        RagAskResponse response =
                ragAnswerService.ask(
                        notebookId,
                        request,
                        "user@example.com"
                );

        assertThat(response.getAnswer())
                .isEqualTo(
                        "Java variable names can contain letters and digits."
                );

        assertThat(response.getSources())
                .hasSize(1);

        assertThat(response.getSources().get(0).getChunkId())
                .isEqualTo("chunk-1");

        ArgumentCaptor<String> promptCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(chatModel)
                .call(
                        promptCaptor.capture()
                );

        assertThat(promptCaptor.getValue())
                .contains(
                        "using only the context",
                        "chunk-1",
                        "Variable names can contain letters and digits.",
                        "What are Java variable naming rules?"
                );
    }

    @Test
    void returnsFallbackAnswerWhenNoChunksAreFound() {
        UUID notebookId =
                UUID.randomUUID();

        when(
                ragSearchService.search(
                        any(UUID.class),
                        any(SemanticSearchRequest.class),
                        any(String.class)
                )
        ).thenReturn(
                List.of()
        );

        RagAskResponse response =
                ragAnswerService.ask(
                        notebookId,
                        request(
                                "What is missing?",
                                5
                        ),
                        "user@example.com"
                );

        assertThat(response.getAnswer())
                .isEqualTo(
                        "I could not find relevant context in this notebook to answer that question."
                );

        assertThat(response.getSources())
                .isEmpty();

        verify(chatModel, never())
                .call(
                        any(String.class)
                );
    }

    private static RagAskRequest request(
            String question,
            Integer topK
    ) {

        RagAskRequest request =
                new RagAskRequest();

        request.setQuestion(
                question
        );

        request.setTopK(
                topK
        );

        return request;
    }
}
