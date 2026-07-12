package com.CRM.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:noreply@crm.app}")
    private String senderEmail;

    @Value("${brevo.sender-name:CRM App}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    /**
     * Sends a plain-text email asynchronously via the Brevo transactional API.
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", body
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent to {} with subject '{}' via Brevo. Response: {}", to, subject, response.getBody());
            } else {
                log.error("Brevo API returned status {} for email to {}: {}", response.getStatusCode(), to, response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {} via Brevo", to, e);
        }
    }

    /**
     * Sends a templated email by replacing placeholder variables.
     * Variables in the template should be in {{variableName}} format.
     */
    @Async
    public void sendTemplatedEmail(String to, String subject, String template,
                                    Map<String, String> variables) {
        String body = template;
        for (var entry : variables.entrySet()) {
            body = body.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        sendEmail(to, subject, body);
    }
}
