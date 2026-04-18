package com.alertsystem.notifier.kafka;

import com.alertsystem.common.events.AlertEvent;
import com.alertsystem.common.kafka.KafkaTopics;
import com.alertsystem.notifier.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaTopics.ALERTS,
            groupId = "online-notifier",
            containerFactory = "alertListenerFactory"
    )
    public void onAlert(AlertEvent event) {
        log.info("Alert received: rule={}, status={}, severity={}",
                event.getRuleName(), event.getStatus(), event.getSeverity());
        try {
            notificationService.processAlert(event);
        } catch (Exception e) {
            log.error("Error processing alert notification: {}", e.getMessage());
        }
    }
}
