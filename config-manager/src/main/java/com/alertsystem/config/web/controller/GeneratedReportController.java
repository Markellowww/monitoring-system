package com.alertsystem.config.web.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.config.entity.GeneratedReport;
import com.alertsystem.config.entity.ReportSchedule;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.repository.GeneratedReportRepository;
import com.alertsystem.config.repository.ReportScheduleRepository;
import com.alertsystem.config.repository.UserRepository;
import com.alertsystem.config.web.dto.request.GeneratedReportRequest;
import com.alertsystem.config.web.dto.response.GeneratedReportResponse;
import com.alertsystem.config.web.mapper.GeneratedReportMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/config/reports")
@RequiredArgsConstructor
public class GeneratedReportController {

    private final GeneratedReportRepository reportRepository;
    private final ReportScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final GeneratedReportMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GeneratedReportResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportRepository.findAllByOrderByCreatedAtDesc(pageable).map(mapper::toResponse)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneratedReportResponse>> getById(@PathVariable UUID id) {
        return reportRepository.findById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(r))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GeneratedReportResponse>> register(
            @Valid @RequestBody GeneratedReportRequest request,
            @RequestHeader("X-User-Id") UUID userId) {

        ReportSchedule schedule = scheduleRepository.findById(request.getScheduleId()).orElse(null);
        User creator = userRepository.findById(userId).orElse(null);

        GeneratedReport report = GeneratedReport.builder()
                .schedule(schedule)
                .periodFrom(request.getPeriodFrom())
                .periodTo(request.getPeriodTo())
                .format(request.getFormat())
                .filePath(request.getFilePath())
                .createdBy(creator)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(reportRepository.save(report))));
    }
}
