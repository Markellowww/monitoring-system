package com.alertsystem.notifier.service;

import com.alertsystem.common.events.AlertEvent;
import com.alertsystem.notifier.entity.NotificationLog;
import com.alertsystem.notifier.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final TelegramSender telegramSender;
    private final EmailSender emailSender;
    private final NotificationLogRepository logRepository;
    private final StringRedisTemplate redisTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${config-manager.url}")
    private String configManagerUrl;

    @Value("${notifier.dedup-ttl-sec:600}")
    private long dedupTtlSec;

    public void processAlert(AlertEvent event) {
        if (event.getNotificationChannelId() == null) {
            log.debug("No notification channel for alert: {}", event.getRuleId());
            return;
        }

        String dedupKey = "dedup:" + event.getRuleId() + ":" + event.getStatus();

        // Дедупликация через Redis
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", Duration.ofSeconds(dedupTtlSec));

        if (Boolean.FALSE.equals(isNew)) {
            log.debug("Skipping duplicate notification for rule: {}", event.getRuleId());
            saveLog(event, NotificationLog.Status.SKIPPED_DEDUP, null, null);
            return;
        }

        Map<String, Object> channelConfig = fetchChannelConfig(event.getNotificationChannelId());
        if (channelConfig == null) {
            log.warn("Channel not found: {}", event.getNotificationChannelId());
            return;
        }

        String channelType = (String) channelConfig.get("type");

        try {
            if ("TELEGRAM".equals(channelType)) {
                sendTelegram(event, channelConfig);
            } else if ("EMAIL".equals(channelType)) {
                sendEmail(event, channelConfig);
            } else {
                log.warn("Unknown channel type: {}", channelType);
                return;
            }
            saveLog(event, NotificationLog.Status.SENT, channelType, null);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            saveLog(event, NotificationLog.Status.FAILED, channelType, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void sendTelegram(AlertEvent event, Map<String, Object> channelConfig) {
        Map<String, Object> config = (Map<String, Object>) channelConfig.get("config");
        String botToken = (String) config.get("bot_token");
        String chatId   = (String) config.get("chat_id");

        String text = telegramSender.formatAlert(
                event.getRuleName(),
                event.getSourceName(),
                event.getStatus().name(),
                event.getCurrentValue(),
                event.getThreshold(),
                event.getSeverity().name()
        );

        telegramSender.send(botToken, chatId, text);
    }

    @SuppressWarnings("unchecked")
    private void sendEmail(AlertEvent event, Map<String, Object> channelConfig) {
        Map<String, Object> config = (Map<String, Object>) channelConfig.get("config");
        String from = (String) config.get("from");
        List<String> to = (List<String>) config.get("to");

        String subject = emailSender.formatAlertSubject(
                event.getRuleName(), event.getStatus().name(), event.getSeverity().name());
        String body = emailSender.formatAlertBody(
                event.getRuleName(), event.getSourceName(),
                event.getStatus().name(), event.getCurrentValue(), event.getThreshold());

        emailSender.send(from, to, subject, body);
    }

    private Map<String, Object> fetchChannelConfig(UUID channelId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(configManagerUrl + "/api/config/notification-channels/{id}", channelId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(resp -> (Map<String, Object>) resp.get("data"))
                    .block();
        } catch (Exception e) {
            log.warn("Could not fetch channel config: {}", e.getMessage());
            return null;
        }
    }

    private void saveLog(AlertEvent event, NotificationLog.Status status,
                         String channelType, String errorMessage) {
        NotificationLog.ChannelType type = null;
        if (channelType != null) {
            try { type = NotificationLog.ChannelType.valueOf(channelType); } catch (Exception ignored) {}
        }

        logRepository.save(NotificationLog.builder()
                .alertId(event.getId())
                .ruleId(event.getRuleId())
                .channelId(event.getNotificationChannelId())
                .channelType(type)
                .status(status)
                .errorMessage(errorMessage)
                .build());
    }
}
