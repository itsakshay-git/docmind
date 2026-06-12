package com.docmind.docmind_api.chat.repository;

import com.docmind.docmind_api.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository
        extends JpaRepository<ChatSession, UUID> {

    Optional<ChatSession> findByNotebookIdAndOwnerEmail(
            UUID notebookId,
            String ownerEmail
    );

    void deleteByNotebookIdAndOwnerEmail(
            UUID notebookId,
            String ownerEmail
    );
}
