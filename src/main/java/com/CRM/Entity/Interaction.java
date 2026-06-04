package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private InteractionType type;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime timestamp;

    @ManyToOne
    private User performedBy;

    @ManyToOne
    private Lead lead;

    @ManyToOne
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account; // The company this lead/contact works for
}