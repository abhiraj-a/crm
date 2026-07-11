package com.CRM.Service;

import com.CRM.DTO.CreateDealRequest;
import com.CRM.DTO.DealResponse;
import com.CRM.DTO.UpdateDealRequest;
import com.CRM.Entity.*;
import com.CRM.Event.DealUpdatedEvent;
import com.CRM.Repo.AccountRepo;
import com.CRM.Repo.DealRepo;
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
public class DealService {

    private final DealRepo dealRepo;
    private final UserRepo userRepo;
    private final LeadRepo leadRepo;
    private final AccountRepo accountRepo;
    private final com.CRM.Repo.TaskRepo taskRepo;
    private final com.CRM.Repo.NoteRepo noteRepo;
    private final ApplicationEventPublisher eventPublisher;

    private User getCurrentUser(String authifyerId) {
        return userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Deal getAuthorizedDeal(UUID dealId, User currentUser) {
        Deal deal = dealRepo.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        if (!deal.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Deal belongs to a different organization.");
        }
        return deal;
    }

    private DealResponse mapToResponse(Deal deal) {
        return DealResponse.builder()
                .id(deal.getId())
                .title(deal.getTitle())
                .value(deal.getValue())
                .stage(deal.getStage())
                .expectedCloseDate(deal.getExpectedCloseDate())
                .createdAt(deal.getCreatedAt())
                .leadId(deal.getLead() != null ? deal.getLead().getId() : null)
                .leadName(deal.getLead() != null ? deal.getLead().getName() : null)
                .assignedToUserId(deal.getAssignedTo() != null ? deal.getAssignedTo().getId() : null)
                .assignedToUserName(deal.getAssignedTo() != null ?
                        deal.getAssignedTo().getFirstName() + " " + deal.getAssignedTo().getLastName() : "Unassigned")
                .accountId(deal.getAccount() != null ? deal.getAccount().getId() : null)
                .accountName(deal.getAccount() != null ? deal.getAccount().getCompanyName() : null)
                .build();
    }

    @Transactional
    public DealResponse createDeal(CreateDealRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);

        Lead lead = request.getLeadId() != null ?
                leadRepo.findById(request.getLeadId()).orElse(null) : null;

        User assignee = request.getAssignedToUserId() != null ?
                userRepo.findById(request.getAssignedToUserId()).orElse(currentUser) : currentUser;

        Account account = request.getAccountId() != null ?
                accountRepo.findById(request.getAccountId()).orElse(null) : null;

        Deal deal = new Deal();
        deal.setTitle(request.getTitle());
        deal.setValue(request.getValue());
        deal.setStage(request.getStage() != null ? request.getStage() : DealStage.PROSPECTING);
        deal.setLead(lead);
        deal.setAssignedTo(assignee);
        deal.setOrganization(currentUser.getOrganization());
        deal.setExpectedCloseDate(request.getExpectedCloseDate());
        deal.setCreatedAt(LocalDateTime.now());
        deal.setAccount(account);

        Deal savedDeal = dealRepo.save(deal);
        return mapToResponse(savedDeal);
    }

    public List<DealResponse> getAllDeals(String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return dealRepo.findByOrganizationId(currentUser.getOrganization().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DealResponse getDealById(UUID dealId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Deal deal = getAuthorizedDeal(dealId, currentUser);
        return mapToResponse(deal);
    }

    @Transactional
    public DealResponse updateDeal(UUID dealId, UpdateDealRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Deal deal = getAuthorizedDeal(dealId, currentUser);

        if (request.getTitle() != null) deal.setTitle(request.getTitle());
        if (request.getValue() != null) deal.setValue(request.getValue());
        if (request.getExpectedCloseDate() != null) deal.setExpectedCloseDate(request.getExpectedCloseDate());

        if (request.getAssignedToUserId() != null) {
            userRepo.findById(request.getAssignedToUserId()).ifPresent(deal::setAssignedTo);
        }
        if (request.getLeadId() != null) {
            leadRepo.findById(request.getLeadId()).ifPresent(deal::setLead);
        }
        if (request.getAccountId() != null) {
            accountRepo.findById(request.getAccountId()).ifPresent(deal::setAccount);
        }

        Deal savedDeal = dealRepo.save(deal);
        return mapToResponse(savedDeal);
    }

    @Transactional
    public DealResponse updateDealStage(UUID dealId, DealStage newStage, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Deal deal = getAuthorizedDeal(dealId, currentUser);

        deal.setStage(newStage);
        Deal savedDeal = dealRepo.save(deal);

        // Fire event for workflow automation when a deal's stage changes
        eventPublisher.publishEvent(new DealUpdatedEvent(this, savedDeal, authifyerId));

        return mapToResponse(savedDeal);
    }

    @Transactional
    public void deleteDeal(UUID dealId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Deal deal = getAuthorizedDeal(dealId, currentUser);
        
        taskRepo.deleteAll(taskRepo.findByRelatedDealId(dealId));
        noteRepo.deleteAll(noteRepo.findByDealIdOrderByCreatedAtDesc(dealId));

        dealRepo.delete(deal);
    }
}
