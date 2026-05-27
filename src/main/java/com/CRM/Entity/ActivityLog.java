package com.CRM.Entity;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
