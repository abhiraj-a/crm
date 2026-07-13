package com.CRM.Service;

import com.CRM.DTO.CalendarEventResponse;
import com.CRM.DTO.CreateCalendarEventRequest;
import com.CRM.DTO.UpdateCalendarEventRequest;
import com.CRM.Entity.CalendarEvent;
import com.CRM.Entity.Deal;
import com.CRM.Entity.Lead;
import com.CRM.Entity.User;
import com.CRM.Entity.Contact;
import com.CRM.Repo.CalendarEventRepo;
import com.CRM.Repo.DealRepo;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.UserRepo;
import com.CRM.Repo.ContactRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class
CalendarEventService {

    private final CalendarEventRepo calendarEventRepo;
    private final UserRepo userRepo;
    private final LeadRepo leadRepo;
    private final DealRepo dealRepo;
    private final ContactRepo contactRepo;

    public List<CalendarEventResponse> getEventsForOrg(String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return calendarEventRepo.findByOrganizationId(user.getOrganization().getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public CalendarEventResponse createEvent(CreateCalendarEventRequest request, String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CalendarEvent event = CalendarEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .organization(user.getOrganization())
                .build();

        if (request.getAssignedToUserId() != null) {
            User assignedTo = userRepo.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            event.setAssignedTo(assignedTo);
        } else {
            event.setAssignedTo(user);
        }

        if (request.getRelatedLeadId() != null) {
            Lead lead = leadRepo.findById(request.getRelatedLeadId())
                    .orElseThrow(() -> new RuntimeException("Lead not found"));
            event.setRelatedLead(lead);
        }

        if (request.getRelatedDealId() != null) {
            Deal deal = dealRepo.findById(request.getRelatedDealId())
                    .orElseThrow(() -> new RuntimeException("Deal not found"));
            event.setRelatedDeal(deal);
        }

        if (request.getRelatedContactId() != null) {
            Contact contact = contactRepo.findById(request.getRelatedContactId())
                    .orElseThrow(() -> new RuntimeException("Contact not found"));
            event.setRelatedContact(contact);
        }

        CalendarEvent saved = calendarEventRepo.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public CalendarEventResponse updateEvent(UUID id, UpdateCalendarEventRequest request, String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CalendarEvent event = calendarEventRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());

        if (request.getAssignedToUserId() != null) {
            User assignedTo = userRepo.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            event.setAssignedTo(assignedTo);
        } else {
            event.setAssignedTo(null);
        }

        if (request.getRelatedLeadId() != null) {
            Lead lead = leadRepo.findById(request.getRelatedLeadId())
                    .orElseThrow(() -> new RuntimeException("Lead not found"));
            event.setRelatedLead(lead);
        } else {
            event.setRelatedLead(null);
        }

        if (request.getRelatedDealId() != null) {
            Deal deal = dealRepo.findById(request.getRelatedDealId())
                    .orElseThrow(() -> new RuntimeException("Deal not found"));
            event.setRelatedDeal(deal);
        } else {
            event.setRelatedDeal(null);
        }

        if (request.getRelatedContactId() != null) {
            Contact contact = contactRepo.findById(request.getRelatedContactId())
                    .orElseThrow(() -> new RuntimeException("Contact not found"));
            event.setRelatedContact(contact);
        } else {
            event.setRelatedContact(null);
        }

        CalendarEvent saved = calendarEventRepo.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteEvent(UUID id, String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CalendarEvent event = calendarEventRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        calendarEventRepo.delete(event);
    }

    private CalendarEventResponse mapToResponse(CalendarEvent event) {
        return CalendarEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .type(event.getType())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .createdAt(event.getCreatedAt())
                .assignedToUserId(event.getAssignedTo() != null ? event.getAssignedTo().getId() : null)
                .assignedToUserName(event.getAssignedTo() != null ? event.getAssignedTo().getFirstName() + " " + event.getAssignedTo().getLastName() : null)
                .relatedLeadId(event.getRelatedLead() != null ? event.getRelatedLead().getId() : null)
                .relatedLeadName(event.getRelatedLead() != null ? event.getRelatedLead().getName() : null)
                .relatedDealId(event.getRelatedDeal() != null ? event.getRelatedDeal().getId() : null)
                .relatedDealTitle(event.getRelatedDeal() != null ? event.getRelatedDeal().getTitle() : null)
                .relatedContactId(event.getRelatedContact() != null ? event.getRelatedContact().getId() : null)
                .relatedContactName(event.getRelatedContact() != null ? event.getRelatedContact().getFirstName() + " " + event.getRelatedContact().getLastName() : null)
                .build();
    }
}
