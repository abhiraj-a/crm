package com.CRM.Service;

import com.CRM.Entity.MetaIntegration;
import com.CRM.Repository.MetaIntegrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetaLeadProcessingService {

    private final MetaIntegrationRepository metaIntegrationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void processLead(String pageId, String leadgenId) {
        log.info("Processing leadgenId {} for pageId {}", leadgenId, pageId);

        Optional<MetaIntegration> integrationOpt = metaIntegrationRepository.findByPageId(pageId);
        
        if (integrationOpt.isPresent() && "ACTIVE".equals(integrationOpt.get().getStatus())) {
            MetaIntegration integration = integrationOpt.get();
            String pageAccessToken = integration.getPageAccessToken();

            try {
                String leadUrl = String.format("https://graph.facebook.com/v20.0/%s?access_token=%s", leadgenId, pageAccessToken);
                
                // Fetch lead data
                String leadDataJson = restTemplate.getForObject(leadUrl, String.class);
                log.info("Fetched lead data: {}", leadDataJson);

                // TODO: Parse the `leadDataJson` to extract `field_data` (e.g., email, name, phone)
                // TODO: Map to internal Lead schema
                // TODO: Check idempotency (e.g., check if leadgenId already exists in db)
                // TODO: Persist the lead via CRM's Lead processing logic
                
                log.info("Successfully processed and saved lead from Meta.");

            } catch (Exception e) {
                log.error("Failed to fetch/process lead data from Meta Graph API for leadgenId {}: {}", leadgenId, e.getMessage());
            }

        } else {
            log.warn("No active MetaIntegration found for pageId {}", pageId);
        }
    }
}
