package com.alertsystem.metrics.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.common.events.MetricsRawEvent;
import com.alertsystem.metrics.kafka.MetricsProducer;
import com.alertsystem.metrics.service.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsPushController {

    private final MetricsProducer metricsProducer;
    private final ScrapingService scrapingService;

    /**
     * Прием push-метрик от внешних агентов.
     * Тело запроса - MetricsRawEvent JSON.
     */
    @PostMapping("/push")
    public ResponseEntity<ApiResponse<Void>> push(@RequestBody MetricsRawEvent event) {
        metricsProducer.send(event);
        return ResponseEntity.ok(ApiResponse.ok("Metrics accepted", null));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> health() {
        return ResponseEntity.ok(ApiResponse.ok(scrapingService.getJobStatuses()));
    }
}
