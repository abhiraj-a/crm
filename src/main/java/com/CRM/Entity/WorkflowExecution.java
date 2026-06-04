package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private WorkFlow workflow;

    // The type and ID of the entity that triggered this execution (e.g., "Lead", UUID)
    private String entityType;
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    private WorkflowExecutionStatus status;

    // The node where execution is currently paused (approval or delay)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paused_at_node_id")
    private WorkFlowNode pausedAtNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
