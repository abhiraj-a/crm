package com.CRM.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
@Entity
@Table(name = "workflow_nodes")
@Data
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
