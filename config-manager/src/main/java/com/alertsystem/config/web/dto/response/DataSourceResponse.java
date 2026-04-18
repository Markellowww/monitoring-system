package com.alertsystem.config.web.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class DataSourceResponse {

    private UUID id;
    private String name;
    private String type;
    private String endpointUrl;
    private int scrapeIntervalSec;
    private Map<String, String> credentials;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private UserRef createdBy;

    @Data
    public static class UserRef {
        private UUID id;
        private String username;
    }
}
