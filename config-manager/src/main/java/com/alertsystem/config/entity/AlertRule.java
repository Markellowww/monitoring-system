package com.alertsystem.config.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    /**
     * Оператор сравнения: >, <, >=, <=
     */
    @Column(nullable = false, length = 4)
    private String condition;

    @Column(nullable = false)
    private double threshold;

    @Column(name = "duration_sec")
    @Builder.Default
    private int durationSec = 60;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "alert_severity")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Severity severity;

    @Column(name = "no_data_alert")
    @Builder.Default
    private boolean noDataAlert = false;

    @Column(name = "no_data_timeout_sec")
    @Builder.Default
    private int noDataTimeoutSec = 300;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id")
    private DataSource dataSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_channel_id")
    private NotificationChannel notificationChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum Severity {
        INFO, WARNING, CRITICAL
    }
}
