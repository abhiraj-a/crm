package com.CRM.Controller;

import com.CRM.DTO.CreateLeadRequest;
import com.CRM.DTO.LeadResponse;
import com.CRM.DTO.UpdateLeadRequest;
import com.CRM.DTO.BulkLeadRequests;
import com.CRM.Entity.Lead;
import com.CRM.Entity.LeadStatus;
import com.CRM.Service.LeadService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lead")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<?> createLead(@RequestBody CreateLeadRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            LeadResponse newLead = leadService.createLead(request, principal.getAuthifyerId());
            return ResponseEntity.ok(newLead);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<LeadResponse>> getAllLeads(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(leadService.getAllLeads(principal.getAuthifyerId()));
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<?> getLeadById(@PathVariable UUID leadId, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(leadService.getLeadById(leadId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{leadId}")
    public ResponseEntity<?> updateLead(@PathVariable UUID leadId, @RequestBody UpdateLeadRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(leadService.updateLead(leadId, request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{leadId}/status")
    public ResponseEntity<?> updateLeadStatus(@PathVariable UUID leadId, @RequestParam LeadStatus status, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(leadService.updateLeadStatus(leadId, status, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{leadId}")
    public ResponseEntity<?> deleteLead(@PathVariable UUID leadId, @AuthenticationPrincipal Principal principal) {
        try {
            leadService.deleteLead(leadId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Lead deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk/delete")
    public ResponseEntity<?> bulkDeleteLeads(@RequestBody BulkLeadRequests.BulkDeleteRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            leadService.bulkDelete(request.getLeadIds(), principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Leads deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk/assign")
    public ResponseEntity<?> bulkAssignLeads(@RequestBody BulkLeadRequests.BulkAssignRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            leadService.bulkAssign(request.getLeadIds(), request.getAssigneeId(), principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Leads assigned successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk/email")
    public ResponseEntity<?> bulkEmailLeads(@RequestBody BulkLeadRequests.BulkEmailRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            leadService.bulkEmail(request.getLeadIds(), request.getSubject(), request.getBody(), principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Bulk email initiated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk/merge")
    public ResponseEntity<?> mergeLeads(@RequestBody BulkLeadRequests.MergeLeadsRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            leadService.mergeLeads(request.getPrimaryLeadId(), request.getSecondaryLeadIds(), principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Leads merged successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
