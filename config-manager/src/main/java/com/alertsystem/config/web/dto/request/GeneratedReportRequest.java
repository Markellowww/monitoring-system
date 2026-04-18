package com.alertsystem.config.web.dto.request;

import com.alertsystem.config.entity.ReportSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class GeneratedReportRequest {

    @NotNull
    private UUID scheduleId;
    @NotNull
    private Instant periodFrom;
    @NotNull
    private Instant periodTo;
    @NotNull
    private ReportSchedule.Format format;
    @NotBlank
    private String filePath;
}
