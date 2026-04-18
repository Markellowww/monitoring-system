package com.alertsystem.notifier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    private final JavaMailSender mailSender;

    public void send(String from, List<String> to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to.toArray(String[]::new));
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.debug("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    public String formatAlertSubject(String ruleName, String status, String severity) {
        return String.format("[%s] Alert %s: %s", severity, status, ruleName);
    }

    public String formatAlertBody(String ruleName, String sourceName,
                                   String status, double value, double threshold) {
        return String.format("""
                Alert Notification
                ------------------
                Rule:      %s
                Source:    %s
                Status:    %s
                Value:     %.4f
                Threshold: %.4f
                """,
                ruleName, sourceName, status, value, threshold);
    }
}
