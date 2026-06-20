package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.common.metrics.AiOperationMetrics;
import com.docmind.docmind_api.rag.dto.RagAskRequest;
import com.docmind.docmind_api.rag.dto.RagAskResponse;
import com.docmind.docmind_api.rag.dto.RagConversationMessage;
import com.docmind.docmind_api.rag.dto.RagSource;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagAnswerService {

    private static final int MAX_MEMORY_MESSAGES = 8;
    private static final String NO_CONTEXT_ANSWER =
            "I could not find relevant context in this notebook to answer that question.";

    private final RagSearchService ragSearchService;
    private final ChatModel chatModel;
    private final AiOperationMetrics aiOperationMetrics;

    public RagAskResponse ask(
            UUID notebookId,
            RagAskRequest request,
            String ownerEmail
    ) {

        return aiOperationMetrics.record(
                "rag.answer",
                "non_streaming",
                () -> askObserved(
                        notebookId,
                        request,
                        ownerEmail
                )
        );
    }

    private RagAskResponse askObserved(
            UUID notebookId,
            RagAskRequest request,
            String ownerEmail
    ) {

        List<RagConversationMessage> memory =
                normalizeMemory(
                        request.getConversationMemory()
                );

        List<SemanticSearchResult> results =
                search(
                        notebookId,
                        request,
                        ownerEmail,
                        memory
                );

        if (results.isEmpty()) {
            return new RagAskResponse(
                    NO_CONTEXT_ANSWER,
                    List.of()
            );
        }

        String answer =
                chatModel.call(
                        buildPrompt(
                                request.getQuestion(),
                                memory,
                                results
                        )
                );

        return new RagAskResponse(
                answer,
                toSources(
                        results
                )
        );
    }

    public RagAnswerStream stream(
            UUID notebookId,
            RagAskRequest request,
            String ownerEmail
    ) {

        return aiOperationMetrics.record(
                "rag.answer",
                "stream_prepare",
                () -> streamObserved(
                        notebookId,
                        request,
                        ownerEmail
                )
        );
    }

    private RagAnswerStream streamObserved(
            UUID notebookId,
            RagAskRequest request,
            String ownerEmail
    ) {

        List<RagConversationMessage> memory =
                normalizeMemory(
                        request.getConversationMemory()
                );

        List<SemanticSearchResult> results =
                search(
                        notebookId,
                        request,
                        ownerEmail,
                        memory
                );

        if (results.isEmpty()) {
            return new RagAnswerStream(
                    aiOperationMetrics.recordFlux(
                            "rag.answer.stream",
                            "no_context",
                            Flux.just(
                                    NO_CONTEXT_ANSWER
                            )
                    ),
                    List.of()
            );
        }

        return new RagAnswerStream(
                aiOperationMetrics.recordFlux(
                        "rag.answer.stream",
                        "gemini",
                        chatModel.stream(
                                buildPrompt(
                                        request.getQuestion(),
                                        memory,
                                        results
                                )
                        )
                ),
                toSources(
                        results
                )
        );
    }
    private List<SemanticSearchResult> search(
            UUID notebookId,
            RagAskRequest request,
            String ownerEmail,
            List<RagConversationMessage> memory
    ) {

        SemanticSearchRequest searchRequest =
                new SemanticSearchRequest();

        searchRequest.setQuestion(
                buildSearchQuestion(
                        request.getQuestion(),
                        memory
                )
        );

        searchRequest.setTopK(
                request.getTopK()
        );

        return ragSearchService.search(
                notebookId,
                searchRequest,
                ownerEmail
        );
    }

    private String buildPrompt(
            String question,
            List<RagConversationMessage> memory,
            List<SemanticSearchResult> results
    ) {

        String conversationMemory =
                buildConversationMemoryText(
                        memory
                );

        String context =
                results.stream()
                        .map(result -> "Source passage"
                                + "\nScore: "
                                + result.getScore()
                                + "\nContent:\n"
                                + result.getContent())
                        .collect(
                                Collectors.joining(
                                        "\n\n---\n\n"
                                )
                        );

        return """
                You are DocMind, a document question-answering assistant.
                Answer the user's question using only the notebook context below.
                Use the recent conversation only to understand follow-up wording, references, and user intent.
                If the notebook context does not contain the answer, say you do not know from the provided document.
                Format the answer as clean Markdown.
                Use short paragraphs and bullet points when the answer has multiple items.
                When the answer includes code, always use fenced Markdown code blocks with a language tag, such as ```java, ```sql, ```json, or ```bash.
                Put explanations outside code blocks.
                Do not include internal IDs, UUIDs, chunk IDs, document IDs, raw scores, or implementation details in the answer.
                Do not mention "source passage" unless the user explicitly asks about sources.

                Recent conversation:
                %s

                Notebook context:
                %s

                Question:
                %s
                """.formatted(
                conversationMemory,
                context,
                question
        );
    }

    private String buildSearchQuestion(
            String question,
            List<RagConversationMessage> memory
    ) {

        if (memory.isEmpty()) {
            return question;
        }

        return "Recent conversation:\n"
                + buildConversationMemoryText(
                memory
        )
                + "\n\nCurrent question:\n"
                + question;
    }

    private String buildConversationMemoryText(
            List<RagConversationMessage> memory
    ) {

        if (memory.isEmpty()) {
            return "No prior messages in this chat.";
        }

        return memory.stream()
                .map(message -> normalizeRole(
                        message.getRole()
                )
                        + ": "
                        + message.getContent())
                .collect(
                        Collectors.joining(
                                "\n"
                        )
                );
    }

    private List<RagSource> toSources(
            List<SemanticSearchResult> results
    ) {

        return results.stream()
                .map(result -> new RagSource(
                        result.getChunkId(),
                        result.getDocumentId(),
                        result.getScore()
                ))
                .toList();
    }

    private List<RagConversationMessage> normalizeMemory(
            List<RagConversationMessage> memory
    ) {

        if (memory == null || memory.isEmpty()) {
            return List.of();
        }

        int start =
                Math.max(
                        memory.size() - MAX_MEMORY_MESSAGES,
                        0
                );

        return memory.subList(
                        start,
                        memory.size()
                )
                .stream()
                .filter(message -> message.getContent() != null
                        && !message.getContent().isBlank())
                .map(message -> new RagConversationMessage(
                        normalizeRole(
                                message.getRole()
                        ),
                        message.getContent()
                                .trim()
                ))
                .toList();
    }

    private String normalizeRole(
            String role
    ) {

        if (role == null || role.isBlank()) {
            return "MESSAGE";
        }

        return role.trim()
                .toUpperCase();
    }
}
