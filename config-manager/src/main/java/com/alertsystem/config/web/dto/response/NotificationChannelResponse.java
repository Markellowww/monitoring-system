package com.alertsystem.config.web.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class NotificationChannelResponse {

    private UUID id;
    private String name;
    private String type;
    private Map<String, Object> config;
    private Instant createdAt;
    private Instant updatedAt;
    private UserRef createdBy;

    @Data
    public static class UserRef {
        private UUID id;
        private String username;
    }
}
