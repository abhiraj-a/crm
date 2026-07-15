package com.CRM.Service;

import com.CRM.DTO.GoogleOAuthTokenResponse;
import com.CRM.DTO.GoogleAdsCustomerListResponse;
import com.CRM.Entity.GoogleAdsIntegration;
import com.CRM.Repo.GoogleAdsIntegrationRepo;
import com.CRM.Util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAdsAuthService {

    private final GoogleAdsIntegrationRepo googleAdsIntegrationRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.ads.client.id:}")
    private String clientId;

    @Value("${google.ads.client.secret:}")
    private String clientSecret;

    @Value("${google.ads.developer.token:}")
    private String developerToken;

    @Value("${google.ads.redirect.uri:}")
    private String redirectUri;

    @Value("${google.ads.login.customer.id:}")
    private String loginCustomerId;

    public String getAuthUrl(String state) {
        return String.format("https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&scope=https://www.googleapis.com/auth/adwords&access_type=offline&prompt=consent&state=%s&response_type=code",
                clientId, redirectUri, state);
    }

    public void processGoogleCallback(String code, String state) {
        Long userId = Long.valueOf(state);

        // 1. Exchange code for user access token
        String tokenUrl = "https://oauth2.googleapis.com/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<GoogleOAuthTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, GoogleOAuthTokenResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            GoogleOAuthTokenResponse tokenResponse = response.getBody();
            
            // 2. We should fetch accessible customers here (simulated or using raw REST for now)
            // For now, we will store a default integration for the MCC customer ID since that's what's typically configured
            
            Optional<GoogleAdsIntegration> existingOpt = googleAdsIntegrationRepo.findByUserIdAndGoogleCustomerId(userId, loginCustomerId);
            GoogleAdsIntegration integration = existingOpt.orElse(new GoogleAdsIntegration());
            
            integration.setUserId(userId);
            integration.setGoogleCustomerId(loginCustomerId);
            integration.setAccessToken(tokenResponse.getAccessToken());
            
            if (tokenResponse.getRefreshToken() != null) {
                integration.setRefreshToken(EncryptionUtil.encrypt(tokenResponse.getRefreshToken()));
            }
            
            if (tokenResponse.getExpiresIn() != null) {
                integration.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
            }
            
            integration.setIsActive(true);
            
            googleAdsIntegrationRepo.save(integration);
            log.info("Successfully connected Google Ads for user {}", userId);
        } else {
            log.error("Failed to exchange code for tokens. Status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to exchange code for tokens");
        }
    }

    public String refreshAccessToken(GoogleAdsIntegration integration) {
        if (integration.getTokenExpiresAt() != null && integration.getTokenExpiresAt().isAfter(LocalDateTime.now().plusMinutes(5))) {
            return integration.getAccessToken(); // Token is still valid
        }
        
        log.info("Refreshing access token for user {}, customer {}", integration.getUserId(), integration.getGoogleCustomerId());
        String tokenUrl = "https://oauth2.googleapis.com/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", EncryptionUtil.decrypt(integration.getRefreshToken()));
        map.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<GoogleOAuthTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, GoogleOAuthTokenResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GoogleOAuthTokenResponse tokenResponse = response.getBody();
                integration.setAccessToken(tokenResponse.getAccessToken());
                if (tokenResponse.getExpiresIn() != null) {
                    integration.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
                }
                googleAdsIntegrationRepo.save(integration);
                return tokenResponse.getAccessToken();
            }
        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            integration.setIsActive(false);
            googleAdsIntegrationRepo.save(integration);
        }
        return null;
    }
    
    public HttpHeaders buildAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("developer-token", developerToken);
        if (loginCustomerId != null && !loginCustomerId.isEmpty()) {
            headers.set("login-customer-id", loginCustomerId);
        }
        return headers;
    }
    
    public boolean getStatus(Long userId) {
        List<GoogleAdsIntegration> integrations = googleAdsIntegrationRepo.findByUserId(userId);
        return integrations.stream().anyMatch(GoogleAdsIntegration::getIsActive);
    }
    
    public void disconnect(Long userId) {
        List<GoogleAdsIntegration> integrations = googleAdsIntegrationRepo.findByUserId(userId);
        for (GoogleAdsIntegration integration : integrations) {
            // In a real app we might also revoke the token by calling Google's revoke endpoint
            googleAdsIntegrationRepo.delete(integration);
        }
    }
}
