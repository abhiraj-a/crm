package com.CRM.Service;

import com.CRM.DTO.CreateAccountRequest;
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

    private Account getAuthorizedAccount(UUID accountId, User currentUser) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (!account.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Account belongs to a different organization.");
        }
        return account;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);

        Account parent = request.getParentAccountId() != null ?
                accountRepo.findById(request.getParentAccountId()).orElse(null) : null;

        Account account = Account.builder()
                .companyName(request.getCompanyName())
                .industry(request.getIndustry())
                .website(request.getWebsite())
                .employeeCount(request.getEmployeeCount())
                .annualRevenue(request.getAnnualRevenue())
                .description(request.getDescription())
                .phone(request.getPhone())
                .email(request.getEmail())
                .pincode(request.getPincode())
                .address(request.getAddress())
                .parentAccount(parent)
                .organization(currentUser.getOrganization())
                .assignedTo(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        return accountRepo.save(account);
    }

    public List<Account> getAllAccounts(String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return accountRepo.findByOrganizationId(currentUser.getOrganization().getId());
    }

    public Account getAccountById(UUID accountId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return getAuthorizedAccount(accountId, currentUser);
    }

    @Transactional
    public void deleteAccount(UUID accountId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Account account = getAuthorizedAccount(accountId, currentUser);
        accountRepo.delete(account);
    }

    // --- HIERARCHY & RELATED DATA FETCHING ---

    public List<Account> getChildAccounts(UUID parentAccountId, String authifyerId) {
        // Verify the user has access to the parent before returning children
        User currentUser = getCurrentUser(authifyerId);
        getAuthorizedAccount(parentAccountId, currentUser);
        return accountRepo.findByParentAccountId(parentAccountId);
    }

    public List<Lead> getRelatedContacts(UUID accountId) {
        return leadRepo.findByAccountId(accountId);
    }

    public List<Deal> getRelatedOpportunities(UUID accountId) {
        return dealRepo.findByAccountId(accountId);
    }
}