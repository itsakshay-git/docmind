package com.docmind.docmind_api.notebook.repository;

import com.docmind.docmind_api.notebook.entity.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotebookRepository
        extends JpaRepository<Notebook, UUID> {

    List<Notebook> findByOwnerEmail(String ownerEmail);

    Optional<Notebook> findByIdAndOwnerEmail(
            UUID id,
            String ownerEmail
    );

    void deleteByIdAndOwnerEmail(
            UUID id,
            String ownerEmail
    );
}
