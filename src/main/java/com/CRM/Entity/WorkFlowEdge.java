package com.CRM.Entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;
@Entity
@Table(name = "workflow_edges")
@Data
public class WorkFlowEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private WorkFlow workflow;

    @ManyToOne
    private WorkFlowNode sourceNode;

    @ManyToOne
    private WorkFlowNode targetNode;

    private String conditionLabel;
}
