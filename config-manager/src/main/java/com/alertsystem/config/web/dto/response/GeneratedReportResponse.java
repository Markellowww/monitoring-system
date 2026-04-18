package com.alertsystem.config.web.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class GeneratedReportResponse {

    private UUID id;
    private ScheduleRef schedule;
    private Instant periodFrom;
    private Instant periodTo;
    private String format;
    private String filePath;
    private Instant createdAt;
    private UserRef createdBy;

    @Data
    public static class ScheduleRef {
        private UUID id;
        private String name;
    }

    @Data
    public static class UserRef {
        private UUID id;
        private String username;
    }
}
