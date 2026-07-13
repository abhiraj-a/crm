package com.CRM.Service;

import com.CRM.DTO.CreateTicketRequest;
import com.CRM.DTO.TicketResponse;
import com.CRM.Entity.*;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.TicketRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepo ticketRepo;
    private final LeadRepo leadRepo;
    private final UserRepo userRepo;

    public List<TicketResponse> getTicketsForOrg(String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ticketRepo.findByOrganizationId(currentUser.getOrganization().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String authifyerId) {
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

        return mapToResponse(ticketRepo.save(ticket));
    }

    @Transactional
    public TicketResponse updateTicketStatus(UUID ticketId, TicketStatus newStatus, String authifyerId) {
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

        return mapToResponse(ticketRepo.save(ticket));
    }

    @Transactional
    public TicketResponse assignTicket(UUID ticketId, UUID assignedToUserId, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Ticket belongs to a different organization.");
        }

        User assignee = userRepo.findById(assignedToUserId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));

        if (!assignee.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Assignee belongs to a different organization.");
        }

        ticket.setAssignedTo(assignee);
        return mapToResponse(ticketRepo.save(ticket));
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .createdAt(ticket.getCreatedAt())
                .closedAt(ticket.getClosedAt())
                .assignedToUserId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToUserName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFirstName() + " " + ticket.getAssignedTo().getLastName() : null)
                .leadId(ticket.getLead() != null ? ticket.getLead().getId() : null)
                .leadName(ticket.getLead() != null ? ticket.getLead().getName() : null)
                .build();
    }
}
