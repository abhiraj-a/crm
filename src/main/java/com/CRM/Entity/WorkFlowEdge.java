package com.CRM.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Entity
@Table(name = "workflow_edges")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
