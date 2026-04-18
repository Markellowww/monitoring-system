package com.alertsystem.tsdb.kafka;

import com.alertsystem.common.events.MetricsRawEvent;
import com.alertsystem.common.kafka.KafkaTopics;
import com.alertsystem.tsdb.service.InfluxDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsConsumer {

    private final InfluxDbService influxDbService;

    @KafkaListener(
            topics = KafkaTopics.METRICS_RAW,
            groupId = "tsdb-storage",
            containerFactory = "metricsListenerFactory"
    )
    public void onMetrics(MetricsRawEvent event) {
        try {
            influxDbService.writeMetrics(event);
        } catch (Exception e) {
            log.error("Failed to write metrics for source {}: {}", event.getSourceName(), e.getMessage());
        }
    }
}
