package com.CRM.Service;

import com.CRM.Entity.*;
import com.CRM.Repo.ApprovalRequestRepo;
import com.CRM.Repo.NotificationRepo;
import com.CRM.Repo.UserRepo;
import com.CRM.Repo.WorkflowExecutionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ApprovalRequestRepo approvalRequestRepo;
    private final WorkflowExecutionRepo workflowExecutionRepo;
    private final WorkflowExecutionEngine executionEngine;
    private final UserRepo userRepo;
    private final NotificationRepo notificationRepo;

    /**
     * Returns all pending approval requests for the current user.
     */
    public List<ApprovalRequest> getPendingApprovals(String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return approvalRequestRepo.findByApproverIdAndStatus(user.getId(), ApprovalStatus.PENDING);
    }

    /**
     * Returns all pending approvals across the organization (for admins).
     */
    public List<ApprovalRequest> getOrganizationPendingApprovals(String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return approvalRequestRepo.findByOrganizationIdAndStatus(
                user.getOrganization().getId(), ApprovalStatus.PENDING);
    }

    /**
     * Approves a pending request and resumes the workflow execution.
     */
    @Transactional
    public ApprovalRequest approve(UUID approvalRequestId, String comments, String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ApprovalRequest request = approvalRequestRepo.findById(approvalRequestId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        // Verify the user is the designated approver
        if (!request.getApprover().getId().equals(user.getId())) {
            // Allow admins to approve on behalf
            if (user.getRole() != Role.ADMIN) {
                throw new RuntimeException("You are not authorized to approve this request");
            }
        }

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("This approval request has already been resolved");
        }

        // Mark as approved
        request.setStatus(ApprovalStatus.APPROVED);
        request.setComments(comments);
        request.setResolvedAt(LocalDateTime.now());
        approvalRequestRepo.save(request);

        // Resume the workflow execution
        WorkflowExecution execution = request.getWorkflowExecution();
        if (execution.getStatus() == WorkflowExecutionStatus.PAUSED_FOR_APPROVAL) {
            executionEngine.resumeFromNode(
                    execution.getPausedAtNode(),
                    execution.getEntityType(),
                    execution.getEntityId(),
                    execution
            );
        }

        log.info("Approval request {} approved by {}", approvalRequestId, user.getFirstName());
        return request;
    }

    /**
     * Rejects a pending request and fails the workflow execution.
     */
    @Transactional
    public ApprovalRequest reject(UUID approvalRequestId, String comments, String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ApprovalRequest request = approvalRequestRepo.findById(approvalRequestId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (!request.getApprover().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("You are not authorized to reject this request");
        }

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("This approval request has already been resolved");
        }

        // Mark as rejected
        request.setStatus(ApprovalStatus.REJECTED);
        request.setComments(comments);
        request.setResolvedAt(LocalDateTime.now());
        approvalRequestRepo.save(request);

        // Fail the workflow execution
        WorkflowExecution execution = request.getWorkflowExecution();
        execution.setStatus(WorkflowExecutionStatus.FAILED);
        execution.setCompletedAt(LocalDateTime.now());
        workflowExecutionRepo.save(execution);

        // Notify the workflow creator
        Notification notification = Notification.builder()
                .title("Approval Rejected: " + request.getRequestTitle())
                .message("Rejected by " + user.getFirstName() + ". Reason: " + comments)
                .isRead(false)
                .user(request.getApprover())
                .organization(request.getOrganization())
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepo.save(notification);

        log.info("Approval request {} rejected by {}", approvalRequestId, user.getFirstName());
        return request;
    }
}
