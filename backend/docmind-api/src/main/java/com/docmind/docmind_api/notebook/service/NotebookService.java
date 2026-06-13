package com.docmind.docmind_api.notebook.service;

import com.docmind.docmind_api.chat.entity.ChatSession;
import com.docmind.docmind_api.chat.repository.ChatMessageRepository;
import com.docmind.docmind_api.chat.repository.ChatSessionRepository;
import com.docmind.docmind_api.document.service.DocumentService;
import com.docmind.docmind_api.notebook.dto.CreateNotebookRequest;
import com.docmind.docmind_api.notebook.dto.NotebookResponse;
import com.docmind.docmind_api.notebook.dto.UpdateNotebookRequest;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotebookService {

    private final NotebookRepository notebookRepository;
    private final DocumentService documentService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public NotebookResponse create(
            CreateNotebookRequest request,
            String ownerEmail
    ) {

        Notebook notebook = new Notebook();

        notebook.setTitle(request.getTitle());
        notebook.setOwnerEmail(ownerEmail);

        Notebook saved =
                notebookRepository.save(notebook);

        return new NotebookResponse(
                saved.getId().toString(),
                saved.getTitle(),
                saved.getCreatedAt(),
                0
        );
    }

    @Transactional(readOnly = true)
    public List<NotebookResponse> getMyNotebooks(
            String ownerEmail
    ) {

        return notebookRepository
                .findByOwnerEmail(ownerEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotebookResponse getNotebook(
            UUID id,
            String ownerEmail
    ) {

        Notebook notebook =
                notebookRepository
                        .findByIdAndOwnerEmail(
                                id,
                                ownerEmail
                        )
                        .orElseThrow();

        return new NotebookResponse(
                notebook.getId().toString(),
                notebook.getTitle(),
                notebook.getCreatedAt(),
                documentService.countDocumentsForNotebook(
                        notebook.getId()
                )
        );
    }

    @Transactional
    public void deleteNotebook(
            UUID id,
            String ownerEmail
    ) {

        Notebook notebook =
                notebookRepository
                        .findByIdAndOwnerEmail(
                                id,
                                ownerEmail
                        )
                        .orElseThrow();

        chatSessionRepository
                .findByNotebookIdAndOwnerEmail(
                        id,
                        ownerEmail
                )
                .ifPresent(session -> deleteChatSession(session));

        documentService.deleteDocumentsForNotebook(
                id
        );

        notebookRepository.delete(notebook);
    }

    @Transactional
    public NotebookResponse updateNotebook(
            UUID id,
            UpdateNotebookRequest request,
            String ownerEmail
    ) {

        Notebook notebook =
                notebookRepository
                        .findByIdAndOwnerEmail(
                                id,
                                ownerEmail
                        )
                        .orElseThrow();

        if (request.getTitle() == null
                || request.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Notebook title is required"
            );
        }

        notebook.setTitle(
                request.getTitle()
                        .trim()
        );

        Notebook saved =
                notebookRepository.save(notebook);

        return new NotebookResponse(
                saved.getId().toString(),
                saved.getTitle(),
                saved.getCreatedAt(),
                documentService.countDocumentsForNotebook(
                        saved.getId()
                )
        );
    }

    private NotebookResponse toResponse(
            Notebook notebook
    ) {

        return new NotebookResponse(
                notebook.getId().toString(),
                notebook.getTitle(),
                notebook.getCreatedAt(),
                documentService.countDocumentsForNotebook(
                        notebook.getId()
                )
        );
    }

    private void deleteChatSession(
            ChatSession session
    ) {

        chatMessageRepository.deleteBySessionId(
                session.getId()
        );

        chatSessionRepository.delete(session);
    }
}
