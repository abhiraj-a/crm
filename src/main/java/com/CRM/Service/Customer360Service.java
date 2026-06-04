package com.CRM.Service;

import com.CRM.DTO.TimeLineEvent;
import com.CRM.Entity.Lead;
import com.CRM.Entity.User;
import com.CRM.Repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Customer360Service {

    private final InteractionRepo interactionRepo;
    private final TicketRepo ticketRepo;
    private final NoteRepo noteRepo;
    private final TaskRepo taskRepo;
    private final LeadRepo leadRepo;
    private final UserRepo userRepo;

    public List<TimeLineEvent> getCustomerTimeline(UUID leadId, String authifyerId) {
        // 1. Security Check
        User currentUser = userRepo.findByAuthifyerId(authifyerId).orElseThrow();
        Lead lead = leadRepo.findById(leadId).orElseThrow();

        if (!lead.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<TimeLineEvent> timeline = new ArrayList<>();

        // 2. Fetch and Map Interactions (Calls, Emails, Meetings)
        interactionRepo.findByLeadIdOrderByTimestampDesc(leadId).forEach(interaction -> {
            timeline.add(TimeLineEvent.builder()
                    .eventId(interaction.getId())
                    .eventType(interaction.getType().name())
                    .title(interaction.getSubject())
                    .description(interaction.getDetails())
                    .timestamp(interaction.getTimestamp())
                    .authorName(interaction.getPerformedBy().getFirstName())
                    .build());
        });

        // 3. Fetch and Map Support Tickets
        ticketRepo.findByLeadIdOrderByCreatedAtDesc(leadId).forEach(ticket -> {
            timeline.add(TimeLineEvent.builder()
                    .eventId(ticket.getId())
                    .eventType("TICKET_" + ticket.getStatus().name())
                    .title("Ticket: " + ticket.getSubject())
                    .description(ticket.getDescription())
                    .timestamp(ticket.getCreatedAt())
                    .authorName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFirstName() : "Unassigned")
                    .build());
        });

        // 4. Fetch and Map Notes
        noteRepo.findByLeadIdOrderByCreatedAtDesc(leadId).forEach(note -> {
            timeline.add(TimeLineEvent.builder()
                    .eventId(note.getId())
                    .eventType("NOTE")
                    .title("Added a Note")
                    .description(note.getContent())
                    .timestamp(note.getCreatedAt())
                    .authorName(note.getCreatedBy().getFirstName())
                    .build());
        });

        // 5. Sort Everything Chronologically (Newest first)
        timeline.sort(Comparator.comparing(TimeLineEvent::getTimestamp).reversed());

        return timeline;
    }
}