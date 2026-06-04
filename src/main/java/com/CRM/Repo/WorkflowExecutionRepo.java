package com.CRM.Repo;

import com.CRM.Entity.WorkflowExecution;
import com.CRM.Entity.WorkflowExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowExecutionRepo extends JpaRepository<WorkflowExecution, UUID> {
    List<WorkflowExecution> findByStatusAndOrganizationId(WorkflowExecutionStatus status, UUID organizationId);
    List<WorkflowExecution> findByEntityTypeAndEntityId(String entityType, UUID entityId);
}
