package com.alertsystem.metrics.kafka;

import com.alertsystem.common.events.ConfigUpdatedEvent;
import com.alertsystem.common.kafka.KafkaTopics;
import com.alertsystem.metrics.service.ConfigSyncService;
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
            groupId = "metrics-collector-config",
            containerFactory = "configUpdatedListenerFactory"
    )
    public void onConfigUpdated(ConfigUpdatedEvent event) {
        if (!"metrics-collector".equals(event.getTargetService())) {
            return;
        }

        log.info("Config update received: type={}, entityId={}", event.getType(), event.getEntityId());

        switch (event.getType()) {
            case DATA_SOURCE_CREATED, DATA_SOURCE_UPDATED ->
                    configSyncService.handleDataSourceUpdate(event.getPayload());
            case DATA_SOURCE_DELETED ->
                    configSyncService.handleDataSourceDelete(event.getEntityId());
            default -> log.debug("Ignored config event type: {}", event.getType());
        }
    }
}
