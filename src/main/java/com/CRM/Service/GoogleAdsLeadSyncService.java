package com.CRM.Service;

import com.CRM.Entity.GoogleAdsIntegration;
import com.CRM.Entity.Lead;
import com.CRM.Entity.LeadStatus;
import com.CRM.Repo.GoogleAdsIntegrationRepo;
import com.CRM.Repo.LeadRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAdsLeadSyncService {

    private final GoogleAdsIntegrationRepo googleAdsIntegrationRepo;
    private final LeadRepo leadRepo;
    private final GoogleAdsAuthService googleAdsAuthService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedDelay = 900000) // Every 15 minutes
    public void syncLeads() {
        log.info("Starting Google Ads lead sync job...");
        List<GoogleAdsIntegration> activeIntegrations = googleAdsIntegrationRepo.findByIsActiveTrue();

        for (GoogleAdsIntegration integration : activeIntegrations) {
            try {
                String accessToken = googleAdsAuthService.refreshAccessToken(integration);
                if (accessToken == null) {
                    log.warn("Skipping sync for user {} due to token refresh failure", integration.getUserId());
                    continue;
                }

                String customerId = integration.getGoogleCustomerId();
                if (customerId == null || customerId.isEmpty()) {
                    continue;
                }

                // Call Google Ads API to fetch lead form submission data
                // In a production app, we would use the google-ads java client.
                // Using raw REST for simulation
                String url = String.format("https://googleads.googleapis.com/v16/customers/%s/googleAds:searchStream", customerId);
                
                String query = "SELECT lead_form_submission_data.id, lead_form_submission_data.lead_form_user_provided_data, lead_form_submission_data.submission_date_time FROM lead_form_submission_data";
                
                HttpHeaders headers = googleAdsAuthService.buildAuthHeaders(accessToken);
                headers.set("Content-Type", "application/json");

                Map<String, String> body = Map.of("query", query);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

                // Note: Real GAQL response processing is more complex, this simulates basic logic
                try {
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
                    log.info("Fetched Google Ads leads for user {}: {}", integration.getUserId(), response.getStatusCode());
                    
                    // Parse response and save leads here...
                    // (Simulated mapping since we don't have a real GAQL response format right now)
                    
                    // Example of deduplication and saving:
                    /*
                     String googleSubmissionId = extractId(response);
                     Optional<Lead> existingLead = leadRepo.findByExternalId(googleSubmissionId);
                     if (existingLead.isEmpty()) {
                         Lead newLead = Lead.builder()
                             .name(...)
                             .email(...)
                             .source("Google Ads")
                             .status(LeadStatus.NEW)
                             .createdAt(LocalDateTime.now())
                             .externalId(googleSubmissionId)
                             .build();
                         leadRepo.save(newLead);
                         log.info("Successfully processed and saved lead from Google Ads.");
                     }
                    */

                } catch (Exception e) {
                    log.error("Failed to fetch lead data from Google Ads API for customer {}: {}", customerId, e.getMessage());
                }

            } catch (Exception e) {
                log.error("Error during lead sync for integration {}: {}", integration.getId(), e.getMessage());
            }
        }
        log.info("Completed Google Ads lead sync job.");
    }
}
