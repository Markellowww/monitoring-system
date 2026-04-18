package com.alertsystem.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdatedEvent {

    private String targetService;
    private EventType type;
    private String entityId;
    private String payload;

    public enum EventType {
        DATA_SOURCE_CREATED,
        DATA_SOURCE_UPDATED,
        DATA_SOURCE_DELETED,
        ALERT_RULE_CREATED,
        ALERT_RULE_UPDATED,
        ALERT_RULE_DELETED,
        NOTIFICATION_CHANNEL_UPDATED,
        REPORT_SCHEDULE_UPDATED
    }
}
