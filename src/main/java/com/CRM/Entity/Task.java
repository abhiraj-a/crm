package com.CRM.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDate deadline;

    @ManyToOne
    private User assignedTo;

    @ManyToOne
    private Deal relatedDeal;

    @ManyToOne
    private Lead relatedLead;

    @ManyToOne
    private Organization organization;

    private LocalDateTime createdAt;
}
