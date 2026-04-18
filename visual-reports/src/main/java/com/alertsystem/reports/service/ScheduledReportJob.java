package com.alertsystem.reports.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Простой планировщик на @Scheduled, проверяет активные расписания из config-manager
 * и запускает генерацию отчетов. В продакшене следует заменить на Quartz с cron-триггерами.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportJob {

    private final ReportGeneratorService reportGeneratorService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${config-manager.url}")
    private String configManagerUrl;

    @Value("${config-manager.url}")
    private String configManagerRegistrationUrl;

    /**
     * Каждый час проверяем, не пора ли генерировать отчеты.
     * (В реальном проекте используйте Quartz CronTrigger для точного cron-расписания.)
     */
    @Scheduled(cron = "0 0 * * * *")
    @SuppressWarnings("unchecked")
    public void checkAndGenerateReports() {
        log.info("Checking report schedules...");

        try {
            List<Map<String, Object>> schedules = webClientBuilder.build()
                    .get()
                    .uri(configManagerUrl + "/api/config/report-schedules")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(resp -> (List<Map<String, Object>>) resp.get("data"))
                    .block();

            if (schedules == null) return;

            for (Map<String, Object> schedule : schedules) {
                Boolean active = (Boolean) schedule.get("active");
                if (!Boolean.TRUE.equals(active)) continue;

                UUID scheduleId = UUID.fromString((String) schedule.get("id"));
                int periodDays = ((Number) schedule.getOrDefault("periodDays", 7)).intValue();
                String format = (String) schedule.getOrDefault("format", "PDF");

                Instant to = Instant.now();
                Instant from = to.minus(periodDays, ChronoUnit.DAYS);

                try {
                    String filePath = reportGeneratorService.generateReport(scheduleId, from, to, format);

                    // Регистрируем отчет в config-manager
                    webClientBuilder.build()
                            .post()
                            .uri(configManagerUrl + "/api/config/reports")
                            .bodyValue(Map.of(
                                    "scheduleId",  scheduleId.toString(),
                                    "periodFrom",  from.toString(),
                                    "periodTo",    to.toString(),
                                    "format",      format,
                                    "filePath",    filePath
                            ))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

                    log.info("Report generated for schedule {}: {}", scheduleId, filePath);
                } catch (Exception e) {
                    log.error("Failed to generate report for schedule {}: {}", scheduleId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching report schedules: {}", e.getMessage());
        }
    }
}
