package com.CRM.Service;

import com.CRM.DTO.CreateTicketRequest;
import com.CRM.Entity.*;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.TicketRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepo ticketRepo;
    private final LeadRepo leadRepo;
    private final UserRepo userRepo;

    /**
     * Creates a support ticket linked to a lead.
     */
    @Transactional
    public Ticket createTicket(CreateTicketRequest request, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lead lead = leadRepo.findById(request.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Lead belongs to a different organization.");
        }

        User assignee = request.getAssignedToUserId() != null ?
                userRepo.findById(request.getAssignedToUserId()).orElse(currentUser) : currentUser;

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .priority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM)
                .assignedTo(assignee)
                .lead(lead)
                .organization(currentUser.getOrganization())
                .createdAt(LocalDateTime.now())
                .build();

        return ticketRepo.save(ticket);
    }

    /**
     * Updates the status of an existing ticket.
     * Sets closedAt when moving to RESOLVED or CLOSED.
     */
    @Transactional
    public Ticket updateTicketStatus(UUID ticketId, TicketStatus newStatus, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Ticket belongs to a different organization.");
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        return ticketRepo.save(ticket);
    }
}
