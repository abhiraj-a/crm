package com.CRM.Controller;

import com.CRM.DTO.CreateInteractionRequest;
import com.CRM.DTO.CreateNoteRequest;
import com.CRM.DTO.CreateTicketRequest;
import com.CRM.Entity.TicketStatus;
import com.CRM.Service.InteractionService;
import com.CRM.Service.NoteService;
import com.CRM.Service.TicketService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer360")
@RequiredArgsConstructor
public class Customer360InputController {

    private final InteractionService interactionService;
    private final TicketService ticketService;
    private final NoteService noteService;

    /**
     * Log a customer interaction (call, meeting, etc.).
     */
    @PostMapping("/interactions")
    public ResponseEntity<?> createInteraction(@RequestBody CreateInteractionRequest request,
                                               @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(interactionService.createInteraction(request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Add a note to a lead and/or deal.
     */
    @PostMapping("/notes")
    public ResponseEntity<?> createNote(@RequestBody CreateNoteRequest request,
                                        @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(noteService.createNote(request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
