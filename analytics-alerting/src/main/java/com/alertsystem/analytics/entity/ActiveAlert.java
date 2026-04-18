package com.alertsystem.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "active_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ActiveAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "source_name")
    private String sourceName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "alert_severity")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "alert_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Status status;

    @Column(name = "current_value")
    private Double currentValue;

    private Double threshold;

    @Column(name = "fired_at")
    private Instant firedAt;

    @Column(name = "last_evaluated_at")
    private Instant lastEvaluatedAt;

    @Column(name = "notification_sent_at")
    private Instant notificationSentAt;

    @Column(name = "notification_channel_id")
    private UUID notificationChannelId;

    public enum Status { FIRING, RESOLVED }

    public enum Severity { INFO, WARNING, CRITICAL }
}
