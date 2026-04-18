package com.alertsystem.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Локальная копия правила алертинга, полученная из config-manager.
 */
@Data
@Builder
public class AlertRuleConfig {
    private UUID id;
    private String name;
    private String metricName;
    private String condition;
    private double threshold;
    private int durationSec;
    private String severity;
    private boolean noDataAlert;
    private int noDataTimeoutSec;
    private boolean active;

    private DataSourceRef dataSource;
    private NotificationChannelRef notificationChannel;

    @Data
    public static class DataSourceRef {
        private UUID id;
        private String name;
    }

    @Data
    public static class NotificationChannelRef {
        private UUID id;
        private String name;
    }
}
