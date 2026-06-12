package com.docmind.docmind_api.chat.repository;

import com.docmind.docmind_api.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findBySessionIdOrderByMessageOrderAsc(
            UUID sessionId
    );

    int countBySessionId(
            UUID sessionId
    );

    void deleteBySessionId(
            UUID sessionId
    );
}
