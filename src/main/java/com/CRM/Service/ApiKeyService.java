package com.CRM.Service;

import com.CRM.DTO.ApiKeyResponse;
import com.CRM.DTO.CreateApiKeyRequest;
import com.CRM.Entity.ApiKey;
import com.CRM.Entity.Organization;
import com.CRM.Entity.User;
import com.CRM.Repo.ApiKeyRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepo apiKeyRepo;
    private final UserRepo userRepo;
    
    private static final String KEY_PREFIX = "crm_";

    @Transactional
    public ApiKeyResponse generateKey(CreateApiKeyRequest request, String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != com.CRM.Entity.Role.ADMIN) {
            throw new RuntimeException("Only admins can manage API keys");
        }

        // Generate 48 random bytes and base64 encode them for a secure key
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[48];
        random.nextBytes(keyBytes);
        String randomStr = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
        
        String rawKey = KEY_PREFIX + randomStr;
        String keyHash = hashKey(rawKey);
        String prefix = rawKey.substring(0, 12); // "crm_XXXXXXXX"

        ApiKey apiKey = ApiKey.builder()
                .keyHash(keyHash)
                .keyPrefix(prefix)
                .name(request.getName())
                .organization(user.getOrganization())
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        ApiKey savedKey = apiKeyRepo.save(apiKey);

        ApiKeyResponse response = mapToResponse(savedKey);
        response.setRawKey(rawKey); // Only return the raw key once upon creation
        return response;
    }

    public List<ApiKeyResponse> getActiveKeys(String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != com.CRM.Entity.Role.ADMIN) {
            throw new RuntimeException("Only admins can manage API keys");
        }

        return apiKeyRepo.findByOrganizationIdAndActiveTrue(user.getOrganization().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeKey(UUID keyId, String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != com.CRM.Entity.Role.ADMIN) {
            throw new RuntimeException("Only admins can manage API keys");
        }

        ApiKey apiKey = apiKeyRepo.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API Key not found"));

        if (!apiKey.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        apiKey.setActive(false);
        apiKeyRepo.save(apiKey);
    }

    @Transactional
    public Organization validateKey(String rawKey) {
        if (rawKey == null || rawKey.isEmpty()) {
            throw new RuntimeException("Invalid API key");
        }
        
        String keyHash = hashKey(rawKey);
        ApiKey apiKey = apiKeyRepo.findByKeyHash(keyHash)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));

        if (!apiKey.isActive()) {
            throw new RuntimeException("API key has been revoked");
        }

        // Update last used timestamp
        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyRepo.save(apiKey);

        return apiKey.getOrganization();
    }

    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not hash API key", e);
        }
    }

    private ApiKeyResponse mapToResponse(ApiKey key) {
        return ApiKeyResponse.builder()
                .id(key.getId())
                .name(key.getName())
                .keyPrefix(key.getKeyPrefix())
                .createdAt(key.getCreatedAt())
                .lastUsedAt(key.getLastUsedAt())
                .active(key.isActive())
                .build();
    }
}
