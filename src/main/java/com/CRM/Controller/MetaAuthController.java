package com.CRM.Controller;

import com.CRM.Service.MetaAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaAuthController {

    private final MetaAuthService metaAuthService;

    @GetMapping("/auth-url")
    public ResponseEntity<String> getAuthUrl(@RequestParam("state") String state) {
        return ResponseEntity.ok(metaAuthService.getAuthUrl(state));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleMetaCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        
        try {
            metaAuthService.processMetaCallback(code, state);
            return ResponseEntity.ok("Successfully connected Meta Pages.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to connect: " + e.getMessage());
        }
    }
}
