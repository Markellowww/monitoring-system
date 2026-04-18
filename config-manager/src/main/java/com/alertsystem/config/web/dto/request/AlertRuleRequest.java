package com.alertsystem.config.web.dto.request;

import com.alertsystem.config.entity.AlertRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AlertRuleRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String metricName;
    @NotBlank
    private String condition;
    @NotNull
    private Double threshold;
    private int durationSec = 60;
    private AlertRule.Severity severity = AlertRule.Severity.WARNING;
    private boolean noDataAlert = false;
    private int noDataTimeoutSec = 300;
    private boolean active = true;
    private UUID dataSourceId;
    private UUID notificationChannelId;

    public AlertRule toEntity() {
        return AlertRule.builder()
                .name(name)
                .metricName(metricName)
                .condition(condition)
                .threshold(threshold)
                .durationSec(durationSec)
                .severity(severity)
                .noDataAlert(noDataAlert)
                .noDataTimeoutSec(noDataTimeoutSec)
                .active(active)
                .build();
    }
}
