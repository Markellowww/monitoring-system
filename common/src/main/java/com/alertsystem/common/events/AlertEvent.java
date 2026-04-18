package com.alertsystem.common.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {

    private UUID id;
    private UUID ruleId;
    private String ruleName;
    private Severity severity;
    private AlertStatus status;
    private String metricName;
    private double currentValue;
    private double threshold;
    private String sourceName;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Instant firedAt;

    private UUID notificationChannelId;

    public enum Severity {
        INFO, WARNING, CRITICAL
    }

    public enum AlertStatus {
        FIRING, RESOLVED
    }
}
