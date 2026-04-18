package com.alertsystem.analytics.kafka;

import com.alertsystem.analytics.service.ConfigSyncService;
import com.alertsystem.common.events.ConfigUpdatedEvent;
import com.alertsystem.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigUpdatedConsumer {

    private final ConfigSyncService configSyncService;

    @KafkaListener(
            topics = KafkaTopics.CONFIG_UPDATED,
            groupId = "analytics-alerting-config",
            containerFactory = "configListenerFactory"
    )
    public void onConfigUpdated(ConfigUpdatedEvent event) {
        if (!"analytics-alerting".equals(event.getTargetService())) {
            return;
        }

        log.info("Config update: type={}, entityId={}", event.getType(), event.getEntityId());

        switch (event.getType()) {
            case ALERT_RULE_CREATED, ALERT_RULE_UPDATED ->
                    configSyncService.handleRuleUpdate(event.getPayload());
            case ALERT_RULE_DELETED ->
                    configSyncService.handleRuleDelete(event.getEntityId());
            default -> log.debug("Ignored: {}", event.getType());
        }
    }
}
