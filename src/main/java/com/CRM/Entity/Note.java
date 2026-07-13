package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notes")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
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
