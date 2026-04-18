package com.alertsystem.reports.service;

import com.alertsystem.reports.model.ReportData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGeneratorService {

    private final PdfReportService pdfReportService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${reports.storage-path:/tmp/reports}")
    private String storagePath;

    @Value("${analytics-alerting.url}")
    private String alertingUrl;

    @Value("${config-manager.url}")
    private String configManagerUrl;

    /**
     * Генерирует отчет за указанный период и сохраняет на диск.
     *
     * @return путь к сохраненному файлу
     */
    public String generateReport(UUID scheduleId, Instant from, Instant to, String format) throws IOException {
        ReportData data = collectData(from, to);

        Path dir = Paths.get(storagePath);
        Files.createDirectories(dir);

        String filename = String.format("report_%s_%s.%s",
                scheduleId, Instant.now().getEpochSecond(),
                "PDF".equals(format) ? "pdf" : "html");

        Path filePath = dir.resolve(filename);

        if ("PDF".equals(format)) {
            byte[] pdf = pdfReportService.generate(data);
            Files.write(filePath, pdf);
        } else {
            String html = generateHtmlReport(data);
            Files.writeString(filePath, html);
        }

        log.info("Report saved: {}", filePath);
        return filePath.toString();
    }

    @SuppressWarnings("unchecked")
    private ReportData collectData(Instant from, Instant to) {
        List<ReportData.AlertSummary> alerts = new ArrayList<>();
        int total = 0, critical = 0, warning = 0;

        try {
            List<Map<String, Object>> historyList = webClientBuilder.build()
                    .get()
                    .uri(alertingUrl + "/api/alerts/history?from={f}&to={t}",
                            from.toString(), to.toString())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(resp -> (List<Map<String, Object>>) resp.get("data"))
                    .block();

            if (historyList != null) {
                for (Map<String, Object> h : historyList) {
                    String severity = (String) h.getOrDefault("severity", "WARNING");
                    alerts.add(ReportData.AlertSummary.builder()
                            .ruleName((String) h.getOrDefault("ruleName", ""))
                            .sourceName((String) h.get("sourceName"))
                            .severity(severity)
                            .firedAt(parseInstant(h.get("firedAt")))
                            .resolvedAt(parseInstant(h.get("resolvedAt")))
                            .maxValue(parseDouble(h.get("maxValue")))
                            .build());

                    total++;
                    if ("CRITICAL".equals(severity)) critical++;
                    if ("WARNING".equals(severity)) warning++;
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch alert history: {}", e.getMessage());
        }

        return ReportData.builder()
                .periodFrom(from)
                .periodTo(to)
                .totalAlerts(total)
                .criticalAlerts(critical)
                .warningAlerts(warning)
                .alerts(alerts)
                .build();
    }

    private String generateHtmlReport(ReportData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Alert Report</title></head><body>");
        sb.append("<h1>Infrastructure Monitoring Report</h1>");
        sb.append("<p>Total alerts: ").append(data.getTotalAlerts()).append("</p>");
        sb.append("<p>Critical: ").append(data.getCriticalAlerts()).append("</p>");
        sb.append("<p>Warning: ").append(data.getWarningAlerts()).append("</p>");
        sb.append("<table border='1'><tr><th>Rule</th><th>Source</th><th>Severity</th><th>Fired At</th></tr>");
        for (ReportData.AlertSummary a : data.getAlerts()) {
            sb.append("<tr><td>").append(a.getRuleName())
              .append("</td><td>").append(a.getSourceName())
              .append("</td><td>").append(a.getSeverity())
              .append("</td><td>").append(a.getFiredAt())
              .append("</td></tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    private Instant parseInstant(Object value) {
        if (value == null) return null;
        try { return Instant.parse(value.toString()); } catch (Exception e) { return null; }
    }

    private Double parseDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return null;
    }
}
