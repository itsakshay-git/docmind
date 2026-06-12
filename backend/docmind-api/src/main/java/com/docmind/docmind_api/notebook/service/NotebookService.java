package com.docmind.docmind_api.notebook.service;

import com.docmind.docmind_api.chat.entity.ChatSession;
import com.docmind.docmind_api.chat.repository.ChatMessageRepository;
import com.docmind.docmind_api.chat.repository.ChatSessionRepository;
import com.docmind.docmind_api.document.service.DocumentService;
import com.docmind.docmind_api.notebook.dto.CreateNotebookRequest;
import com.docmind.docmind_api.notebook.dto.NotebookResponse;
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
                saved.getTitle()
        );
    }

    @Transactional(readOnly = true)
    public List<NotebookResponse> getMyNotebooks(
            String ownerEmail
    ) {

        return notebookRepository
                .findByOwnerEmail(ownerEmail)
                .stream()
                .map(n -> new NotebookResponse(
                        n.getId().toString(),
                        n.getTitle()
                ))
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
                notebook.getTitle()
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

    private void deleteChatSession(
            ChatSession session
    ) {

        chatMessageRepository.deleteBySessionId(
                session.getId()
        );

        chatSessionRepository.delete(session);
    }
}
