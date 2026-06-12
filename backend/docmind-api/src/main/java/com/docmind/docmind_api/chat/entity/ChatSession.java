package com.docmind.docmind_api.chat.entity;

import com.docmind.docmind_api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chat_sessions")
public class ChatSession extends BaseEntity {

    @Column(name = "notebook_id", nullable = false)
    private UUID notebookId;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(nullable = false)
    private String title;
}
