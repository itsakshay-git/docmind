package com.docmind.docmind_api.rag.controller;

import com.docmind.docmind_api.rag.dto.RagAskRequest;
import com.docmind.docmind_api.rag.dto.RagAskResponse;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import com.docmind.docmind_api.rag.service.RagAnswerService;
import com.docmind.docmind_api.rag.service.RagSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagSearchService ragSearchService;
    private final RagAnswerService ragAnswerService;

    @PostMapping("/notebooks/{notebookId}/search")
    public List<SemanticSearchResult> search(
            @PathVariable UUID notebookId,
            @RequestBody SemanticSearchRequest request,
            Authentication authentication
    ) {

        return ragSearchService.search(
                notebookId,
                request,
                authentication.getName()
        );
    }

    @PostMapping("/notebooks/{notebookId}/ask")
    public RagAskResponse ask(
            @PathVariable UUID notebookId,
            @RequestBody RagAskRequest request,
            Authentication authentication
    ) {

        return ragAnswerService.ask(
                notebookId,
                request,
                authentication.getName()
        );
    }
}
