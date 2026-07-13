package com.CRM.Controller;

import com.CRM.DTO.ExternalLeadRequest;
import com.CRM.DTO.ExternalLeadResponse;
import com.CRM.Entity.Lead;
import com.CRM.Entity.LeadStatus;
import com.CRM.Entity.Organization;
import com.CRM.Repo.LeadRepo;
import com.CRM.Service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/external")
@RequiredArgsConstructor
public class ExternalLeadController {

    private final ApiKeyService apiKeyService;
    private final LeadRepo leadRepo;

    @PostMapping("/lead")
    public ResponseEntity<?> createLeadFromExternal(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ExternalLeadRequest request) {
        try {
            // Validate the key and get the associated organization
            Organization organization = apiKeyService.validateKey(apiKey);

            // Create lead
            Lead lead = Lead.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .company(request.getCompany())
                    .source(request.getSource() != null ? request.getSource() : "External API")
                    .status(LeadStatus.CONTACTED)
                    .score(0) // Default score
                    .createdAt(LocalDateTime.now())
                    .organization(organization)
                    // Unassigned by default when created via API
                    .assignedTo(null) 
                    .account(null)
                    .build();

            // Append message to notes if provided (ideally this would go to a Note entity, 
            // but for simplicity we'll just append it to source or keep it simple. 
            // For a robust system, we would create a Note entity linked to the Lead)
            
            Lead savedLead = leadRepo.save(lead);
            
            // TODO: In a real system we should also create a Note for the message 
            // if we want to store the "Contact Us" message body.
            
            ExternalLeadResponse response = ExternalLeadResponse.builder()
                    .leadId(savedLead.getId())
                    .name(savedLead.getName())
                    .email(savedLead.getEmail())
                    .message("Lead created successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Invalid API key") || e.getMessage().contains("revoked")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
