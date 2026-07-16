package com.CRM.Controller;

import com.CRM.DTO.CreateTicketRequest;
import com.CRM.DTO.TicketResponse;
import com.CRM.Entity.TicketStatus;
import com.CRM.Service.TicketService;
import com.CRM.Util.Principal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable UUID ticketId, @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId, principal.getAuthifyerId()));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request,
                                          @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(ticketService.createTicket(request, principal.getAuthifyerId()));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(@PathVariable UUID ticketId,
                                                @RequestParam TicketStatus status,
                                                @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(ticketId, status, principal.getAuthifyerId()));
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponse> assignTicket(@PathVariable UUID ticketId,
                                          @RequestParam UUID assignedToUserId,
                                          @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(ticketService.assignTicket(ticketId, assignedToUserId, principal.getAuthifyerId()));
    }
}
