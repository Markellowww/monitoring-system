package com.alertsystem.config.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ReportSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "cron_expr", nullable = false, length = 64)
    private String cronExpr;

    @Column(name = "period_days")
    @Builder.Default
    private int periodDays = 7;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "report_format")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Format format;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> recipients;

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

    public enum Format {
        PDF, HTML
    }
}
