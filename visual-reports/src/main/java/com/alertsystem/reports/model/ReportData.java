package com.alertsystem.reports.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ReportData {

    private Instant periodFrom;
    private Instant periodTo;
    private int totalAlerts;
    private int criticalAlerts;
    private int warningAlerts;

    private List<AlertSummary> alerts;

    @Data
    @Builder
    public static class AlertSummary {
        private String ruleName;
        private String sourceName;
        private String severity;
        private Instant firedAt;
        private Instant resolvedAt;
        private Double maxValue;
    }
}
