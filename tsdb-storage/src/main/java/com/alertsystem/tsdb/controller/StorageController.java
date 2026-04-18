package com.alertsystem.tsdb.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.tsdb.service.InfluxDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final InfluxDbService influxDbService;

    /**
     * Данные метрики за произвольный период.
     * GET /api/storage/query/range?metric=cpu_usage_percent&sourceId=...&from=...&to=...
     */
    @GetMapping("/query/range")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> queryRange(
            @RequestParam String metric,
            @RequestParam String sourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        List<Map<String, Object>> result = influxDbService.queryRange(metric, sourceId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Последнее известное значение метрики.
     * GET /api/storage/query/latest?metric=cpu_usage_percent&sourceId=...
     */
    @GetMapping("/query/latest")
    public ResponseEntity<ApiResponse<Double>> queryLatest(
            @RequestParam String metric,
            @RequestParam String sourceId) {

        Optional<Double> value = influxDbService.queryLatest(metric, sourceId);
        return value.map(v -> ResponseEntity.ok(ApiResponse.ok(v)))
                .orElse(ResponseEntity.ok(ApiResponse.ok(null)));
    }

    /**
     * Среднее за последние N секунд (для движка алертинга).
     * GET /api/storage/query/avg?metric=cpu_usage_percent&sourceId=...&seconds=60
     */
    @GetMapping("/query/avg")
    public ResponseEntity<ApiResponse<Double>> queryAvg(
            @RequestParam String metric,
            @RequestParam String sourceId,
            @RequestParam(defaultValue = "60") int seconds) {

        var avg = influxDbService.queryAvg(metric, sourceId, seconds);
        return avg.isPresent()
                ? ResponseEntity.ok(ApiResponse.ok(avg.getAsDouble()))
                : ResponseEntity.ok(ApiResponse.ok(null));
    }
}
