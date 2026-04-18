package com.alertsystem.common.kafka;

public final class KafkaTopics {

    public static final String METRICS_RAW = "metrics-raw";
    public static final String ALERTS = "alerts-topic";
    public static final String CONFIG_UPDATED = "config-updated";

    private KafkaTopics() {
    }
}
