package com.alertsystem.config.web.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.config.entity.ReportSchedule;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.repository.ReportScheduleRepository;
import com.alertsystem.config.repository.UserRepository;
import com.alertsystem.config.web.dto.request.ReportScheduleRequest;
import com.alertsystem.config.web.dto.response.ReportScheduleResponse;
import com.alertsystem.config.web.mapper.ReportScheduleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/config/report-schedules")
@RequiredArgsConstructor
public class ReportScheduleController {

    private final ReportScheduleRepository repository;
    private final UserRepository userRepository;
    private final ReportScheduleMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportScheduleResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponseList(repository.findAll())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportScheduleResponse>> getById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(s -> ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(s))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<ReportScheduleResponse>> create(
            @Valid @RequestBody ReportScheduleRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        User creator = userRepository.findById(userId).orElseThrow();
        ReportSchedule schedule = request.toEntity();
        schedule.setCreatedBy(creator);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(repository.save(schedule))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<ReportScheduleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReportScheduleRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setCronExpr(request.getCronExpr());
            existing.setPeriodDays(request.getPeriodDays());
            existing.setFormat(request.getFormat());
            existing.setRecipients(request.getRecipients());
            existing.setActive(request.isActive());
            return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(repository.save(existing))));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
