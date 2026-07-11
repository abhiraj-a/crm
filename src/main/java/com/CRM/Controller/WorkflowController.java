package com.CRM.Controller;

import com.CRM.DTO.CreateWorkflowRequest;
import com.CRM.DTO.WorkflowResponse;
import com.CRM.Service.WorkflowService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * Create a new workflow with nodes and edges.
     */
    @PostMapping
    public ResponseEntity<?> createWorkflow(
            @RequestBody CreateWorkflowRequest request,
            @AuthenticationPrincipal Principal principal) {
        try {
            WorkflowResponse response = workflowService.createWorkflow(request, principal.getAuthifyerId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all workflows for the current organization.
     */
    @GetMapping
    public ResponseEntity<List<com.CRM.DTO.WorkflowSummaryResponse>> getAllWorkflows(
            @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(workflowService.getAllWorkflows(principal.getAuthifyerId()));
    }

    /**
     * Get a single workflow by ID with all nodes and edges.
     */
    @GetMapping("/{workflowId}")
    public ResponseEntity<?> getWorkflowById(
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(workflowService.getWorkflowById(workflowId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Toggle a workflow's active/inactive status.
     */
    @PatchMapping("/{workflowId}/toggle")
    public ResponseEntity<?> toggleWorkflow(
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(workflowService.toggleWorkflow(workflowId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a workflow and all associated nodes and edges.
     */
    @DeleteMapping("/{workflowId}")
    public ResponseEntity<?> deleteWorkflow(
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal Principal principal) {
        try {
            workflowService.deleteWorkflow(workflowId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Workflow deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
