package com.CRM.Service;

import com.CRM.DTO.CreateLeadRequest;
import com.CRM.DTO.LeadResponse;
import com.CRM.DTO.UpdateLeadRequest;
import com.CRM.Entity.Lead;
import com.CRM.Entity.LeadStatus;
import com.CRM.Entity.User;
import com.CRM.Event.LeadCreatedEvent;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepo leadRepo;
    private final UserRepo userRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Lead lead = Lead.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .company(request.getCompany())
                .source(request.getSource())
                .status(LeadStatus.CONTACTED)
                // Use the provided score, or default to 0 if left blank
                .score(request.getScore() != null ? request.getScore() : 0)
                .createdAt(LocalDateTime.now())
                .organization(currentUser.getOrganization())
                .assignedTo(currentUser) // Default assignment
                .build();

        Lead savedLead = leadRepo.save(lead);

        // Broadcast the event to the rest of the application
        eventPublisher.publishEvent(new LeadCreatedEvent(this, savedLead, authifyerId));

        return mapToResponse(savedLead);
    }

    private User getCurrentUser(String authifyerId) {
        return userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Lead getAuthorizedLead(UUID leadId, User currentUser) {
        Lead lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        if (!lead.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Lead belongs to a different organization.");
        }
        return lead;
    }

    private LeadResponse mapToResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .company(lead.getCompany())
                .source(lead.getSource())
                .status(lead.getStatus())
                .score(lead.getScore())
                .createdAt(lead.getCreatedAt())
                // Safely extract just the strings/IDs we need from the User entity
                .assignedToUserId(lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null)
                .assignedToUserName(lead.getAssignedTo() != null ?
                        lead.getAssignedTo().getFirstName() + " " + lead.getAssignedTo().getLastName() : "Unassigned")
                .build();
    }

    public List<LeadResponse> getAllLeads(String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return leadRepo.findByOrganizationId(currentUser.getOrganization().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LeadResponse getLeadById(UUID leadId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Lead lead = getAuthorizedLead(leadId, currentUser);
        return mapToResponse(lead);
    }

    @Transactional
    public LeadResponse updateLead(UUID leadId, UpdateLeadRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Lead lead = getAuthorizedLead(leadId, currentUser);

        if (request.getName() != null) lead.setName(request.getName());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getPhone() != null) lead.setPhone(request.getPhone());
        if (request.getCompany() != null) lead.setCompany(request.getCompany());
        if (request.getScore() != null) lead.setScore(request.getScore());

        Lead updatedLead = leadRepo.save(lead);
        return mapToResponse(updatedLead);
    }

    @Transactional
    public LeadResponse updateLeadStatus(UUID leadId, LeadStatus newStatus, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Lead lead = getAuthorizedLead(leadId, currentUser);
        lead.setStatus(newStatus);
        Lead updatedLead = leadRepo.save(lead);
        return mapToResponse(updatedLead);
    }

    @Transactional
    public void deleteLead(UUID leadId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Lead lead = getAuthorizedLead(leadId, currentUser);
        leadRepo.delete(lead);
    }

}
