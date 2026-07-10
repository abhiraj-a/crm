package com.CRM.Entity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Entity
@Table(name = "workflow_nodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private WorkFlow workflow;

    @Enumerated(EnumType.STRING)
    private NodeType nodeType;

    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String configuration;

    private Double positionX;

    private Double positionY;
}
