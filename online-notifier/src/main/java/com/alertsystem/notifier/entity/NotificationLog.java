package com.alertsystem.notifier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "alert_id", nullable = false)
    private UUID alertId;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "channel_id")
    private UUID channelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", columnDefinition = "channel_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ChannelType channelType;

    @CreationTimestamp
    @Column(name = "sent_at")
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "notification_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Status status;

    @Column(name = "error_message")
    private String errorMessage;

    public enum ChannelType { TELEGRAM, EMAIL }

    public enum Status { SENT, FAILED, SKIPPED_DEDUP }
}
