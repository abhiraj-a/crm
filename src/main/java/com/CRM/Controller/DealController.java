package com.CRM.Controller;

import com.CRM.DTO.CreateDealRequest;
import com.CRM.DTO.DealResponse;
import com.CRM.DTO.UpdateDealRequest;
import com.CRM.Entity.DealStage;
import com.CRM.Service.DealService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    /**
     * Create a new deal in the pipeline.
     */
    @PostMapping
    public ResponseEntity<?> createDeal(@RequestBody CreateDealRequest request,
                                        @AuthenticationPrincipal Principal principal) {
        try {
            DealResponse response = dealService.createDeal(request, principal.getAuthifyerId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all deals for the current organization.
     */
    @GetMapping
    public ResponseEntity<List<DealResponse>> getAllDeals(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(dealService.getAllDeals(principal.getAuthifyerId()));
    }

    /**
     * Get a single deal by ID.
     */
    @GetMapping("/{dealId}")
    public ResponseEntity<?> getDealById(@PathVariable UUID dealId,
                                         @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(dealService.getDealById(dealId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update a deal's general information (not stage).
     */
    @PutMapping("/{dealId}")
    public ResponseEntity<?> updateDeal(@PathVariable UUID dealId,
                                        @RequestBody UpdateDealRequest request,
                                        @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(dealService.updateDeal(dealId, request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Move a deal through the pipeline by updating its stage.
     * This fires a DealUpdatedEvent for workflow automation.
     */
    @PatchMapping("/{dealId}/stage")
    public ResponseEntity<?> updateDealStage(@PathVariable UUID dealId,
                                             @RequestParam DealStage stage,
                                             @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(dealService.updateDealStage(dealId, stage, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a deal from the pipeline.
     */
    @DeleteMapping("/{dealId}")
    public ResponseEntity<?> deleteDeal(@PathVariable UUID dealId,
                                        @AuthenticationPrincipal Principal principal) {
        try {
            dealService.deleteDeal(dealId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Deal deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
