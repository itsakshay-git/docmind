package com.docmind.docmind_api.notebook.controller;

import com.docmind.docmind_api.notebook.dto.CreateNotebookRequest;
import com.docmind.docmind_api.notebook.dto.NotebookResponse;
import com.docmind.docmind_api.notebook.dto.UpdateNotebookRequest;
import com.docmind.docmind_api.notebook.service.NotebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notebooks")
@RequiredArgsConstructor
public class NotebookController {

    private final NotebookService notebookService;

    @PostMapping
    public NotebookResponse create(
            @RequestBody CreateNotebookRequest request,
            Authentication authentication
    ) {

        return notebookService.create(
                request,
                authentication.getName()
        );
    }


    @GetMapping
    public List<NotebookResponse> getMyNotebooks(
            Authentication authentication
    ) {

        return notebookService.getMyNotebooks(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    public NotebookResponse getNotebook(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        return notebookService.getNotebook(
                id,
                authentication.getName()
        );
    }

    @DeleteMapping("/{id}")
    public void deleteNotebook(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        notebookService.deleteNotebook(
                id,
                authentication.getName()
        );
    }

    @PatchMapping("/{id}")
    public NotebookResponse updateNotebook(
            @PathVariable UUID id,
            @RequestBody UpdateNotebookRequest request,
            Authentication authentication
    ) {

        return notebookService.updateNotebook(
                id,
                request,
                authentication.getName()
        );
    }
}
