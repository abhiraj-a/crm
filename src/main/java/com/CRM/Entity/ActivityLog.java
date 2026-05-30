package com.CRM.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue
    private UUID id;

    private String entityType;

    private UUID entityId;

    private String action;

    @ManyToOne
    private User performedBy;

    @ManyToOne
    private Organization organization;

    private LocalDateTime createdAt;
}
