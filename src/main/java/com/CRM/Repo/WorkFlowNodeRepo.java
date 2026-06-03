package com.CRM.Repo;

import com.CRM.Entity.WorkFlowNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkFlowNodeRepo extends JpaRepository<WorkFlowNode, UUID> {
    // Finds the starting point of the workflow
    WorkFlowNode findByWorkflowIdAndNodeType(UUID workflowId, com.CRM.Entity.NodeType nodeType);
}
