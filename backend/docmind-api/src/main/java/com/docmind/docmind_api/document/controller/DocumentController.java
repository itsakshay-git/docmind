package com.docmind.docmind_api.document.controller;

import com.docmind.docmind_api.document.dto.AddUrlSourceRequest;
import com.docmind.docmind_api.document.dto.AddYouTubeTranscriptRequest;
import com.docmind.docmind_api.document.dto.DocumentResponse;
import com.docmind.docmind_api.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(
            value = "/{notebookId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String upload(
            @PathVariable UUID notebookId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        return documentService.upload(
                notebookId,
                file,
                authentication.getName()
        );
    }

    @PostMapping("/{notebookId}/web-url")
    public DocumentResponse addWebUrl(
            @PathVariable UUID notebookId,
            @RequestBody AddUrlSourceRequest request,
            Authentication authentication
    ) {

        return documentService.addWebUrl(
                notebookId,
                request.getUrl(),
                authentication.getName()
        );
    }

    @PostMapping("/{notebookId}/youtube")
    public DocumentResponse addYouTube(
            @PathVariable UUID notebookId,
            @RequestBody AddUrlSourceRequest request,
            Authentication authentication
    ) {

        return documentService.addYouTube(
                notebookId,
                request.getUrl(),
                authentication.getName()
        );
    }

    @PostMapping("/{notebookId}/youtube-transcript")
    public DocumentResponse addYouTubeTranscript(
            @PathVariable UUID notebookId,
            @RequestBody AddYouTubeTranscriptRequest request,
            Authentication authentication
    ) {

        return documentService.addYouTubeTranscript(
                notebookId,
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<DocumentResponse> getMyDocuments(
            Authentication authentication
    ) {

        return documentService.getMyDocuments(
                authentication.getName()
        );
    }

    @GetMapping("/notebooks/{notebookId}")
    public List<DocumentResponse> getNotebookDocuments(
            @PathVariable UUID notebookId,
            Authentication authentication
    ) {

        return documentService.getNotebookDocuments(
                notebookId,
                authentication.getName()
        );
    }

    @DeleteMapping("/{documentId}")
    public void deleteDocument(
            @PathVariable UUID documentId,
            Authentication authentication
    ) throws IOException {

        documentService.deleteDocument(
                documentId,
                authentication.getName()
        );
    }
}
