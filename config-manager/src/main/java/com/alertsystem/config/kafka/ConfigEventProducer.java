package com.alertsystem.config.kafka;

import com.alertsystem.common.events.ConfigUpdatedEvent;
import com.alertsystem.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigEventProducer {

    private final KafkaTemplate<String, ConfigUpdatedEvent> kafkaTemplate;

    public void publish(ConfigUpdatedEvent event) {
        kafkaTemplate.send(KafkaTopics.CONFIG_UPDATED, event.getTargetService(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish config event: {}", ex.getMessage());
                    } else {
                        log.debug("Config event published: type={}, target={}, entityId={}",
                                event.getType(), event.getTargetService(), event.getEntityId());
                    }
                });
    }
}
