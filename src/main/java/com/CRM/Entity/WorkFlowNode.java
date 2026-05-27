package com.CRM.Entity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "workflow_nodes")
public class WorkFlowNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private WorkFlow workflow;

    @Enumerated(EnumType.STRING)
    private NodeType nodeType;

    @Column(columnDefinition = "jsonb")
    private String configuration;

    private Double positionX;

    private Double positionY;
}
