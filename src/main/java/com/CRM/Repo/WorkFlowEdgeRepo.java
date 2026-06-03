package com.CRM.Repo;

import com.CRM.Entity.WorkFlowEdge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowEdgeRepo extends JpaRepository<WorkFlowEdge, UUID> {
    // Finds the next connected blocks in the flow
    List<WorkFlowEdge> findBySourceNodeId(UUID sourceNodeId);
}
