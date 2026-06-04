package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notes")
@Builder
@Data
public class Note {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private Lead lead;

    @ManyToOne
    private Deal deal;

    @ManyToOne
    private Organization organization;

    private LocalDateTime createdAt;
}
