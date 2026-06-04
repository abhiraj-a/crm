package com.CRM.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends a plain-text email asynchronously.
     * Runs on a separate thread so the workflow engine isn't blocked waiting for SMTP.
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@crm.app");

            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a templated email by replacing placeholder variables.
     * Variables in the template should be in {{variableName}} format.
     */
    @Async
    public void sendTemplatedEmail(String to, String subject, String template,
                                    java.util.Map<String, String> variables) {
        String body = template;
        for (var entry : variables.entrySet()) {
            body = body.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        sendEmail(to, subject, body);
    }
}
