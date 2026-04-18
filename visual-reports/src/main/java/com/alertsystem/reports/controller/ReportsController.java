package com.alertsystem.reports.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.reports.service.ReportGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportsController {

    private final ReportGeneratorService reportGeneratorService;
    private final WebClient.Builder webClientBuilder;

    @Value("${tsdb-storage.url}")
    private String tsdbUrl;

    @Value("${analytics-alerting.url}")
    private String alertingUrl;

    /**
     * Данные для дашборда: активные алерты + статистика.
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        try {
            List<?> activeAlerts = webClientBuilder.build()
                    .get()
                    .uri(alertingUrl + "/api/alerts/active")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(resp -> (List<?>) resp.get("data"))
                    .block();

            Map<String, Object> summary = Map.of(
                    "activeAlerts", activeAlerts != null ? activeAlerts.size() : 0,
                    "activeAlertsList", activeAlerts != null ? activeAlerts : List.of()
            );
            return ResponseEntity.ok(ApiResponse.ok(summary));
        } catch (Exception e) {
            log.error("Failed to get dashboard summary: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.ok(Map.of("activeAlerts", 0)));
        }
    }

    /**
     * Данные метрик за последний период для графиков.
     * GET /api/reports/dashboard/metrics?metric=cpu_usage_percent&sourceId=...&hours=24
     */
    @GetMapping("/dashboard/metrics")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMetrics(
            @RequestParam String metric,
            @RequestParam String sourceId,
            @RequestParam(defaultValue = "24") int hours) {

        Instant to   = Instant.now();
        Instant from = to.minus(hours, ChronoUnit.HOURS);

        try {
            List<Map<String, Object>> data = webClientBuilder.build()
                    .get()
                    .uri(tsdbUrl + "/api/storage/query/range?metric={m}&sourceId={s}&from={f}&to={t}",
                            metric, sourceId, from.toString(), to.toString())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(resp -> (List<Map<String, Object>>) resp.get("data"))
                    .block();

            return ResponseEntity.ok(ApiResponse.ok(data != null ? data : List.of()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.ok(List.of()));
        }
    }

    /**
     * История алертов за период.
     */
    @GetMapping("/dashboard/alerts")
    public ResponseEntity<ApiResponse<List<?>>> getAlertHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        List<?> history = webClientBuilder.build()
                .get()
                .uri(alertingUrl + "/api/alerts/history?from={f}&to={t}",
                        from.toString(), to.toString())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(resp -> (List<?>) resp.get("data"))
                .block();

        return ResponseEntity.ok(ApiResponse.ok(history != null ? history : List.of()));
    }

    /**
     * Ручная генерация отчета.
     */
    @PostMapping("/generate/{scheduleId}")
    public ResponseEntity<ApiResponse<String>> generate(
            @PathVariable UUID scheduleId,
            @RequestParam(defaultValue = "7") int periodDays,
            @RequestParam(defaultValue = "PDF") String format) {

        Instant to   = Instant.now();
        Instant from = to.minus(periodDays, ChronoUnit.DAYS);

        try {
            String filePath = reportGeneratorService.generateReport(scheduleId, from, to, format);
            return ResponseEntity.ok(ApiResponse.ok("Report generated: " + filePath));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    /**
     * Скачать сгенерированный файл отчета.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String filePath) {
        try {
            byte[] content = Files.readAllBytes(Paths.get(filePath));
            boolean isPdf = filePath.endsWith(".pdf");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + Paths.get(filePath).getFileName() + "\"")
                    .contentType(isPdf ? MediaType.APPLICATION_PDF : MediaType.TEXT_HTML)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
