package com.docmind.docmind_api.document.repository;

import com.docmind.docmind_api.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

public interface DocumentRepository
        extends JpaRepository<Document, UUID> {

    List<Document> findByNotebookId(UUID notebookId);

    List<Document> findByNotebookIdIn(Collection<UUID> notebookIds);

    long countByNotebookId(UUID notebookId);
}
