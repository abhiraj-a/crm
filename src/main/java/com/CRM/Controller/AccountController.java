package com.CRM.Controller;

import com.CRM.DTO.CreateAccountRequest;
import com.CRM.Entity.Account;
import com.CRM.Service.AccountService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Create a new B2B account (company).
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request,
                                           @AuthenticationPrincipal Principal principal) {
        try {
            Account account = accountService.createAccount(request, principal.getAuthifyerId());
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all accounts for the current organization.
     */
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(accountService.getAllAccounts(principal.getAuthifyerId()));
    }

    /**
     * Get a single account by ID.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable UUID accountId,
                                            @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(accountService.getAccountById(accountId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all subsidiary (child) accounts of a parent account.
     */
    @GetMapping("/{accountId}/children")
    public ResponseEntity<?> getChildAccounts(@PathVariable UUID accountId,
                                              @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(accountService.getChildAccounts(accountId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all leads/contacts linked to this account.
     */
    @GetMapping("/{accountId}/contacts")
    public ResponseEntity<?> getRelatedContacts(@PathVariable UUID accountId,
                                                @AuthenticationPrincipal Principal principal) {
        try {
            // Verify access via getAccountById first
            accountService.getAccountById(accountId, principal.getAuthifyerId());
            return ResponseEntity.ok(accountService.getRelatedContacts(accountId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all deals/opportunities linked to this account.
     */
    @GetMapping("/{accountId}/deals")
    public ResponseEntity<?> getRelatedDeals(@PathVariable UUID accountId,
                                             @AuthenticationPrincipal Principal principal) {
        try {
            accountService.getAccountById(accountId, principal.getAuthifyerId());
            return ResponseEntity.ok(accountService.getRelatedOpportunities(accountId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete an account.
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccount(@PathVariable UUID accountId,
                                           @AuthenticationPrincipal Principal principal) {
        try {
            accountService.deleteAccount(accountId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
