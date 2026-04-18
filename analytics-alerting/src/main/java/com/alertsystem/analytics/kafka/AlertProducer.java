package com.alertsystem.analytics.kafka;

import com.alertsystem.common.events.AlertEvent;
import com.alertsystem.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertProducer {

    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;

    public void send(AlertEvent event) {
        kafkaTemplate.send(KafkaTopics.ALERTS, event.getRuleId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send alert event: {}", ex.getMessage());
                    } else {
                        log.debug("Alert sent: ruleId={}, status={}", event.getRuleId(), event.getStatus());
                    }
                });
    }
}
