package com.docmind.docmind_api.document.controller;

import com.docmind.docmind_api.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return documentService.upload(
                notebookId,
                file
        );
    }

    @GetMapping("/test")
    public String test() {
        return "OK";
    }
}
