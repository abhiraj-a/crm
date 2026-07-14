package com.CRM.Controller;

import com.CRM.DTO.MetaWebhookPayload;
import com.CRM.Service.MetaLeadProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/webhooks/facebook")
@RequiredArgsConstructor
@Slf4j
public class MetaWebhookController {

    private final MetaLeadProcessingService metaLeadProcessingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${meta.webhook.verify-token:}")
    private String verifyToken;

    @Value("${meta.app.secret:}")
    private String appSecret;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verified successfully.");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawPayload) {
        
        // 1. Verify Signature
        if (!isValidSignature(signature, rawPayload)) {
            log.warn("Invalid signature received in Meta webhook");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // 2. Parse Payload
            MetaWebhookPayload payload = objectMapper.readValue(rawPayload, MetaWebhookPayload.class);
            
            // 3. Process Payload Asynchronously
            if ("page".equals(payload.getObject()) && payload.getEntry() != null) {
                for (MetaWebhookPayload.Entry entry : payload.getEntry()) {
                    if (entry.getChanges() != null) {
                        for (MetaWebhookPayload.Change change : entry.getChanges()) {
                            if ("leadgen".equals(change.getField()) && change.getValue() != null) {
                                String leadgenId = change.getValue().getLeadgenId();
                                String pageId = change.getValue().getPageId();

                                if (leadgenId != null && pageId != null) {
                                    metaLeadProcessingService.processLead(pageId, leadgenId);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Meta webhook payload", e);
        }

        // 4. Immediately return 200 OK
        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    private boolean isValidSignature(String signature, String payload) {
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }

        String expectedHash = signature.substring(7);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return expectedHash.equals(hexString.toString());
        } catch (Exception e) {
            log.error("Error computing HMAC SHA256", e);
            return false;
        }
    }
}
