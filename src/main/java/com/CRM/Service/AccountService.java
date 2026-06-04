package com.CRM.Service;

import com.CRM.Entity.Account;
import com.CRM.Entity.Deal;
import com.CRM.Entity.Lead;
import com.CRM.Entity.User;
import com.CRM.Repo.AccountRepo;
import com.CRM.Repo.DealRepo;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepo accountRepo;
    private final UserRepo userRepo;
    private final LeadRepo leadRepo;
    private final DealRepo dealRepo;

    private User getCurrentUser(String authifyerId) {
        return userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public Account createAccount(String companyName, String industry, UUID parentAccountId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);

        Account parent = parentAccountId != null ?
                accountRepo.findById(parentAccountId).orElse(null) : null;

        Account account = Account.builder()
                .companyName(companyName)
                .industry(industry)
                .parentAccount(parent)
                .organization(currentUser.getOrganization())
                .assignedTo(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        return accountRepo.save(account);
    }

    // --- HIERARCHY & RELATED DATA FETCHING ---

    public List<Account> getChildAccounts(UUID parentAccountId, String authifyerId) {
        // In a real app, verify tenant isolation here before returning
        return accountRepo.findByParentAccountId(parentAccountId);
    }

    public List<Lead> getRelatedContacts(UUID accountId) {
        // You would need to add findByAccountId in LeadRepo
        return leadRepo.findByAccountId(accountId);
    }

    public List<Deal> getRelatedOpportunities(UUID accountId) {
        // You would need to add findByAccountId in DealRepo
        return dealRepo.findByAccountId(accountId);
    }
}