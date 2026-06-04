package com.CRM.Controller;

import com.CRM.Entity.ApprovalRequest;
import com.CRM.Service.ApprovalService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /**
     * Get all pending approval requests for the current user.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalRequest>> getPendingApprovals(
            @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(approvalService.getPendingApprovals(principal.getAuthifyerId()));
    }

    /**
     * Get all pending approvals across the organization.
     */
    @GetMapping("/organization/pending")
    public ResponseEntity<List<ApprovalRequest>> getOrganizationPendingApprovals(
            @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(approvalService.getOrganizationPendingApprovals(principal.getAuthifyerId()));
    }

    /**
     * Approve a pending request. Resumes the paused workflow.
     */
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable UUID approvalId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal Principal principal) {
        try {
            String comments = body != null ? body.getOrDefault("comments", "") : "";
            ApprovalRequest result = approvalService.approve(approvalId, comments, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of(
                    "message", "Approval request approved successfully",
                    "approvalId", result.getId().toString(),
                    "status", result.getStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reject a pending request. Stops the workflow execution.
     */
    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<?> reject(
            @PathVariable UUID approvalId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal Principal principal) {
        try {
            String comments = body != null ? body.getOrDefault("comments", "") : "";
            ApprovalRequest result = approvalService.reject(approvalId, comments, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of(
                    "message", "Approval request rejected",
                    "approvalId", result.getId().toString(),
                    "status", result.getStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
