package com.CRM.Controller;

import com.CRM.DTO.ApiKeyResponse;
import com.CRM.DTO.CreateApiKeyRequest;
import com.CRM.Service.ApiKeyService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<?> generateKey(@RequestBody CreateApiKeyRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            ApiKeyResponse response = apiKeyService.generateKey(request, principal.getAuthifyerId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getKeys(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(apiKeyService.getActiveKeys(principal.getAuthifyerId()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<?> revokeKey(@PathVariable UUID keyId, @AuthenticationPrincipal Principal principal) {
        try {
            apiKeyService.revokeKey(keyId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "API key revoked successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
