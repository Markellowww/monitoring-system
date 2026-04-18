package com.alertsystem.config.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "data_sources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "datasource_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Type type;

    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "scrape_interval_sec")
    @Builder.Default
    private int scrapeIntervalSec = 30;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> credentials;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum Type {
        HTTP_SCRAPE, PUSH, SNMP
    }
}
