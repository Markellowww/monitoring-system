package com.alertsystem.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "rule_name")
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "alert_severity")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ActiveAlert.Severity severity;

    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "notifications_count")
    @Builder.Default
    private int notificationsCount = 0;

    @Column(name = "source_name")
    private String sourceName;
}
