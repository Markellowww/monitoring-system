package com.alertsystem.config.web.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ReportScheduleResponse {

    private UUID id;
    private String name;
    private String cronExpr;
    private int periodDays;
    private String format;
    private List<String> recipients;
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
