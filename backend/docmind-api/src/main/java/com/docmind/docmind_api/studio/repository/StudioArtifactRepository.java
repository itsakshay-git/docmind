package com.docmind.docmind_api.studio.repository;

import com.docmind.docmind_api.studio.entity.StudioArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudioArtifactRepository
        extends JpaRepository<StudioArtifact, UUID> {

    List<StudioArtifact> findByNotebookIdAndOwnerEmailOrderByCreatedAtDesc(
            UUID notebookId,
            String ownerEmail
    );

    Optional<StudioArtifact> findByIdAndOwnerEmail(
            UUID id,
            String ownerEmail
    );
}
