package com.alertsystem.config.web.dto.request;

import com.alertsystem.config.entity.ReportSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReportScheduleRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String cronExpr;
    private int periodDays = 7;
    @NotNull
    private ReportSchedule.Format format;
    private List<String> recipients;
    private boolean active = true;

    public ReportSchedule toEntity() {
        return ReportSchedule.builder()
                .name(name)
                .cronExpr(cronExpr)
                .periodDays(periodDays)
                .format(format)
                .recipients(recipients)
                .active(active)
                .build();
    }
}
