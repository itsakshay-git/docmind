package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.rag.dto.RagAskRequest;
import com.docmind.docmind_api.rag.dto.RagAskResponse;
import com.docmind.docmind_api.rag.dto.RagSource;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagAnswerService {

    private final RagSearchService ragSearchService;
    private final ChatModel chatModel;

    public RagAskResponse ask(
            UUID notebookId,
            RagAskRequest request,
            String ownerEmail
    ) {

        SemanticSearchRequest searchRequest =
                new SemanticSearchRequest();

        searchRequest.setQuestion(
                request.getQuestion()
        );

        searchRequest.setTopK(
                request.getTopK()
        );

        List<SemanticSearchResult> results =
                ragSearchService.search(
                        notebookId,
                        searchRequest,
                        ownerEmail
                );

        if (results.isEmpty()) {
            return new RagAskResponse(
                    "I could not find relevant context in this notebook to answer that question.",
                    List.of()
            );
        }

        String answer =
                chatModel.call(
                        buildPrompt(
                                request.getQuestion(),
                                results
                        )
                );

        List<RagSource> sources =
                results.stream()
                        .map(result -> new RagSource(
                                result.getChunkId(),
                                result.getDocumentId(),
                                result.getScore()
                        ))
                        .toList();

        return new RagAskResponse(
                answer,
                sources
        );
    }

    private String buildPrompt(
            String question,
            List<SemanticSearchResult> results
    ) {

        String context =
                results.stream()
                        .map(result -> "Chunk ID: "
                                + result.getChunkId()
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
                Answer the user's question using only the context below.
                If the context does not contain the answer, say you do not know from the provided document.
                Keep the answer concise and cite relevant chunk IDs when useful.

                Context:
                %s

                Question:
                %s
                """.formatted(
                context,
                question
        );
    }
}
