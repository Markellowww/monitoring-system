package com.alertsystem.config.web.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AlertRuleResponse {

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
    private Instant createdAt;
    private Instant updatedAt;
    private DataSourceRef dataSource;
    private NotificationChannelRef notificationChannel;

    @Data
    public static class DataSourceRef {
        private UUID id;
        private String name;
        private String type;
    }

    @Data
    public static class NotificationChannelRef {
        private UUID id;
        private String name;
        private String type;
    }
}
