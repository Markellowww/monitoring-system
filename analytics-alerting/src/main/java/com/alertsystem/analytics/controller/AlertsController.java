package com.alertsystem.analytics.controller;

import com.alertsystem.analytics.entity.ActiveAlert;
import com.alertsystem.analytics.entity.AlertHistory;
import com.alertsystem.analytics.repository.ActiveAlertRepository;
import com.alertsystem.analytics.repository.AlertHistoryRepository;
import com.alertsystem.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertsController {

    private final ActiveAlertRepository activeAlertRepo;
    private final AlertHistoryRepository historyRepo;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ActiveAlert>>> getActive() {
        return ResponseEntity.ok(ApiResponse.ok(
                activeAlertRepo.findAllByStatus(ActiveAlert.Status.FIRING)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ActiveAlert>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(activeAlertRepo.findAllByOrderByFiredAtDesc()));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AlertHistory>>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(ApiResponse.ok(
                historyRepo.findAllByFiredAtBetweenOrderByFiredAtDesc(from, to)));
    }
}
