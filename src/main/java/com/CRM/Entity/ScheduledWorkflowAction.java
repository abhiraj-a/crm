package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_workflow_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledWorkflowAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id")
    private WorkflowExecution workflowExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_from_node_id")
    private WorkFlowNode resumeFromNode;

    // The entity this workflow is operating on
    private String entityType;
    private UUID entityId;

    private LocalDateTime scheduledTime;
    private boolean executed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private LocalDateTime createdAt;
}
