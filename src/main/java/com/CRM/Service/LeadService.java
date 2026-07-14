package com.CRM.Service;

import com.CRM.DTO.CreateLeadRequest;
import com.CRM.DTO.LeadResponse;
import com.CRM.DTO.UpdateLeadRequest;
import com.CRM.DTO.ConvertLeadRequest;
import com.CRM.DTO.ConvertLeadResponse;
import com.CRM.Entity.Account;
import com.CRM.Entity.Contact;
import com.CRM.Entity.Deal;
import com.CRM.Entity.DealStage;
import com.CRM.Entity.Lead;
import com.CRM.Entity.LeadStatus;
import com.CRM.Entity.User;
import com.CRM.Event.LeadCreatedEvent;
import com.CRM.Repo.AccountRepo;
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
    private final AccountRepo accountRepo;
    private final com.CRM.Repo.TaskRepo taskRepo;
    private final com.CRM.Repo.DealRepo dealRepo;
    private final com.CRM.Repo.NoteRepo noteRepo;
    private final com.CRM.Repo.InteractionRepo interactionRepo;
    private final com.CRM.Repo.TicketRepo ticketRepo;
    private final com.CRM.Repo.ContactRepo contactRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);

        // Resolve the assignee: use provided userId if given, otherwise default to current user
        User assignee = currentUser;
        if (request.getAssignedToUserId() != null) {
            if (currentUser.getRole() == com.CRM.Entity.Role.SALES_REP && !request.getAssignedToUserId().equals(currentUser.getId())) {
                throw new RuntimeException("Sales Reps cannot assign leads to other users.");
            }
            assignee = userRepo.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            // Ensure the assignee belongs to the same organization
            if (!assignee.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Cannot assign lead to a user in a different organization.");
            }
        }

        // Resolve the account affiliation if an accountId is provided
        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepo.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            if (!account.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Cannot affiliate lead with an account from a different organization.");
            }
        }

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
                .assignedTo(assignee)
                .account(account)
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
                // Account affiliation info
                .accountId(lead.getAccount() != null ? lead.getAccount().getId() : null)
                .accountName(lead.getAccount() != null ? lead.getAccount().getCompanyName() : null)
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
        if (request.getSource() != null) lead.setSource(request.getSource());
        if (request.getStatus() != null) lead.setStatus(request.getStatus());
        if (request.getScore() != null) lead.setScore(request.getScore());

        if (request.getAssignedToUserId() != null) {
            if (currentUser.getRole() == com.CRM.Entity.Role.SALES_REP && (lead.getAssignedTo() == null || !request.getAssignedToUserId().equals(lead.getAssignedTo().getId()))) {
                throw new RuntimeException("Sales Reps cannot assign leads to other users.");
            }
            User assignee = userRepo.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            if (!assignee.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Cannot assign lead to a user in a different organization.");
            }
            lead.setAssignedTo(assignee);
        }

        if (request.getAccountId() != null) {
            Account account = accountRepo.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            if (!account.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Cannot affiliate lead with an account from a different organization.");
            }
            lead.setAccount(account);
        }

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
        
        taskRepo.deleteAll(taskRepo.findByRelatedLeadId(leadId));
        dealRepo.deleteAll(dealRepo.findByLeadId(leadId));
        noteRepo.deleteAll(noteRepo.findByLeadIdOrderByCreatedAtDesc(leadId));
        interactionRepo.deleteAll(interactionRepo.findByLeadIdOrderByTimestampDesc(leadId));
        ticketRepo.deleteAll(ticketRepo.findByLeadIdOrderByCreatedAtDesc(leadId));

        leadRepo.delete(lead);
    }

    @Transactional
    public void bulkDelete(List<UUID> leadIds, String authifyerId) {
        for (UUID id : leadIds) {
            deleteLead(id, authifyerId);
        }
    }

    @Transactional
    public void bulkAssign(List<UUID> leadIds, UUID assigneeId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        if (currentUser.getRole() == com.CRM.Entity.Role.SALES_REP) {
            throw new RuntimeException("Sales Reps cannot bulk assign leads.");
        }
        User assignee = userRepo.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));
        if (!assignee.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Cannot assign lead to a user in a different organization.");
        }

        for (UUID id : leadIds) {
            Lead lead = getAuthorizedLead(id, currentUser);
            lead.setAssignedTo(assignee);
            leadRepo.save(lead);
        }
    }

    public void bulkEmail(List<UUID> leadIds, String subject, String body, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        for (UUID id : leadIds) {
            Lead lead = getAuthorizedLead(id, currentUser);
            if (lead.getEmail() != null && !lead.getEmail().isEmpty()) {
                emailService.sendEmail(lead.getEmail(), subject, body);
            }
        }
    }

    @Transactional
    public void mergeLeads(UUID primaryLeadId, List<UUID> secondaryLeadIds, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Lead primaryLead = getAuthorizedLead(primaryLeadId, currentUser);

        for (UUID secondaryId : secondaryLeadIds) {
            if (secondaryId.equals(primaryLeadId)) continue;
            
            Lead secondaryLead = getAuthorizedLead(secondaryId, currentUser);

            // Reassign tasks
            taskRepo.findByRelatedLeadId(secondaryId).forEach(task -> {
                task.setRelatedLead(primaryLead);
                taskRepo.save(task);
            });

            // Reassign deals
            dealRepo.findByLeadId(secondaryId).forEach(deal -> {
                deal.setLead(primaryLead);
                dealRepo.save(deal);
            });

            // Reassign notes
            noteRepo.findByLeadIdOrderByCreatedAtDesc(secondaryId).forEach(note -> {
                note.setLead(primaryLead);
                noteRepo.save(note);
            });

            // Reassign interactions
            interactionRepo.findByLeadIdOrderByTimestampDesc(secondaryId).forEach(interaction -> {
                interaction.setLead(primaryLead);
                interactionRepo.save(interaction);
            });

            // Reassign tickets
            ticketRepo.findByLeadIdOrderByCreatedAtDesc(secondaryId).forEach(ticket -> {
                ticket.setLead(primaryLead);
                ticketRepo.save(ticket);
            });

            // Delete secondary lead
            leadRepo.delete(secondaryLead);
        }
    }

    @Transactional
    public ConvertLeadResponse convertLead(UUID leadId, ConvertLeadRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Lead lead = getAuthorizedLead(leadId, currentUser);
        
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new RuntimeException("Lead is already converted");
        }

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepo.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            if (!account.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Cannot affiliate with an account from a different organization.");
            }
        } else if (request.getNewAccountName() != null && !request.getNewAccountName().isBlank()) {
            account = new Account();
            account.setCompanyName(request.getNewAccountName());
            account.setOrganization(currentUser.getOrganization());
            account.setCreatedAt(LocalDateTime.now());
            account = accountRepo.save(account);
        }

        // Split name into first and last name
        String[] nameParts = lead.getName().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        Contact contact = Contact.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .jobTitle(lead.getCompany())
                .account(account)
                .organization(currentUser.getOrganization())
                .assignedTo(lead.getAssignedTo())
                .createdAt(LocalDateTime.now())
                .build();
        
        contact = contactRepo.save(contact);

        Deal deal = null;
        if (request.isCreateDeal()) {
            deal = new Deal();
            deal.setTitle(request.getDealName() != null && !request.getDealName().isBlank() ? request.getDealName() : lead.getName() + " Deal");
            deal.setValue(request.getDealValue() != null ? request.getDealValue() : 0.0);
            deal.setStage(DealStage.PROSPECTING);
            deal.setAssignedTo(lead.getAssignedTo());
            deal.setOrganization(currentUser.getOrganization());
            deal.setCreatedAt(LocalDateTime.now());
            deal.setAccount(account);
            // Link the deal to the lead? (Deal has lead reference in DB)
            // deal.setLead(lead); // actually, maybe not necessary if it's converted, but we can set it.
            deal = dealRepo.save(deal);
        }

        lead.setStatus(LeadStatus.CONVERTED);
        leadRepo.save(lead);

        return ConvertLeadResponse.builder()
                .contactId(contact.getId())
                .accountId(account != null ? account.getId() : null)
                .dealId(deal != null ? deal.getId() : null)
                .build();
    }
}
