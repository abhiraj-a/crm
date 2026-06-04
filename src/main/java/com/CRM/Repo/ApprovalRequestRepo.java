package com.CRM.Repo;

import com.CRM.Entity.ApprovalRequest;
import com.CRM.Entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalRequestRepo extends JpaRepository<ApprovalRequest, UUID> {
    List<ApprovalRequest> findByApproverIdAndStatus(UUID approverId, ApprovalStatus status);
    List<ApprovalRequest> findByOrganizationIdAndStatus(UUID organizationId, ApprovalStatus status);
    List<ApprovalRequest> findByWorkflowExecutionId(UUID workflowExecutionId);
}
