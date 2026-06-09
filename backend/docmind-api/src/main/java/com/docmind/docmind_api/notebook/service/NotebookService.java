package com.docmind.docmind_api.notebook.service;

import com.docmind.docmind_api.notebook.dto.CreateNotebookRequest;
import com.docmind.docmind_api.notebook.dto.NotebookResponse;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotebookService {

    private final NotebookRepository notebookRepository;

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

        notebookRepository.delete(notebook);
    }
}
