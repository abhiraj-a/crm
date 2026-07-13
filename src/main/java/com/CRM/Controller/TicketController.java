package com.CRM.Controller;

import com.CRM.DTO.CreateTicketRequest;
import com.CRM.DTO.TicketResponse;
import com.CRM.Entity.TicketStatus;
import com.CRM.Service.TicketService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getTickets(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(ticketService.getTicketsForOrg(principal.getAuthifyerId()));
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody CreateTicketRequest request,
                                          @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(ticketService.createTicket(request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable UUID ticketId,
                                                @RequestParam TicketStatus status,
                                                @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(ticketService.updateTicketStatus(ticketId, status, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<?> assignTicket(@PathVariable UUID ticketId,
                                          @RequestParam UUID assignedToUserId,
                                          @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(ticketService.assignTicket(ticketId, assignedToUserId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
