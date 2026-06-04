package com.CRM.Repo;

import com.CRM.Entity.WorkFlowNode;
import com.CRM.Entity.NodeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowNodeRepo extends JpaRepository<WorkFlowNode, UUID> {
    // Finds the starting point of the workflow
    WorkFlowNode findByWorkflowIdAndNodeType(UUID workflowId, NodeType nodeType);

    // Finds all nodes belonging to a workflow
    List<WorkFlowNode> findByWorkflowId(UUID workflowId);
}
