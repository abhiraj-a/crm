package com.CRM.Controller;

import com.CRM.Service.GoogleAdsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/google-ads")
@RequiredArgsConstructor
public class GoogleAdsAuthController {

    private final GoogleAdsAuthService googleAdsAuthService;

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl(@RequestParam("state") String state) {
        Map<String, String> response = new HashMap<>();
        response.put("url", googleAdsAuthService.getAuthUrl(state));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        
        try {
            googleAdsAuthService.processGoogleCallback(code, state);
            return ResponseEntity.ok("Successfully connected Google Ads.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to connect: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus(@RequestParam("userId") Long userId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("connected", googleAdsAuthService.getStatus(userId));
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/disconnect")
    public ResponseEntity<String> disconnect(@RequestParam("userId") Long userId) {
        try {
            googleAdsAuthService.disconnect(userId);
            return ResponseEntity.ok("Successfully disconnected Google Ads.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to disconnect: " + e.getMessage());
        }
    }
}
