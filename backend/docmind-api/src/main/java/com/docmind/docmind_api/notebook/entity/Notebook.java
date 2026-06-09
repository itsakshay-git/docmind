package com.docmind.docmind_api.notebook.entity;

import com.docmind.docmind_api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notebooks")
public class Notebook extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;
}
