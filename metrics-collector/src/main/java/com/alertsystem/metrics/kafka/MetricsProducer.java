package com.alertsystem.metrics.kafka;

import com.alertsystem.common.events.MetricsRawEvent;
import com.alertsystem.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsProducer {

    private final KafkaTemplate<String, MetricsRawEvent> kafkaTemplate;

    public void send(MetricsRawEvent event) {
        kafkaTemplate.send(KafkaTopics.METRICS_RAW, event.getSourceId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send metrics event: {}", ex.getMessage());
                    }
                });
    }
}
