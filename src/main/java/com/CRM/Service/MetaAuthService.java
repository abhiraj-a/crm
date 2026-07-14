package com.CRM.Service;

import com.CRM.DTO.MetaAccountsResponse;
import com.CRM.DTO.MetaOAuthResponse;
import com.CRM.Entity.MetaIntegration;
import com.CRM.Repository.MetaIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetaAuthService {

    private final MetaIntegrationRepository metaIntegrationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${meta.app.id:}")
    private String appId;

    @Value("${meta.app.secret:}")
    private String appSecret;

    @Value("${meta.redirect.uri:}")
    private String redirectUri;

    public void processMetaCallback(String code, String state) {
        Long userId = Long.valueOf(state);

        // 1. Exchange code for user access token
        String tokenUrl = String.format("https://graph.facebook.com/v20.0/oauth/access_token?client_id=%s&redirect_uri=%s&client_secret=%s&code=%s",
                appId, redirectUri, appSecret, code);

        MetaOAuthResponse tokenResponse = restTemplate.getForObject(tokenUrl, MetaOAuthResponse.class);

        if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
            String userAccessToken = tokenResponse.getAccessToken();

            // 2. Fetch user's pages
            String accountsUrl = "https://graph.facebook.com/v20.0/me/accounts?access_token=" + userAccessToken;
            MetaAccountsResponse accountsResponse = restTemplate.getForObject(accountsUrl, MetaAccountsResponse.class);

            if (accountsResponse != null && accountsResponse.getData() != null) {
                // 3. Upsert pages into database
                for (MetaAccountsResponse.PageData pageData : accountsResponse.getData()) {
                    Optional<MetaIntegration> existing = metaIntegrationRepository.findByPageId(pageData.getId());

                    MetaIntegration integration = existing.orElse(new MetaIntegration());
                    integration.setUserId(userId);
                    integration.setPageId(pageData.getId());
                    integration.setPageName(pageData.getName());
                    integration.setPageAccessToken(pageData.getAccessToken());
                    integration.setStatus("ACTIVE");

                    metaIntegrationRepository.save(integration);
                }
            }
        }
    }
}
