package com.alertsystem.config.web.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.config.entity.AlertRule;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.repository.UserRepository;
import com.alertsystem.config.service.AlertRuleService;
import com.alertsystem.config.web.dto.request.AlertRuleRequest;
import com.alertsystem.config.web.dto.response.AlertRuleResponse;
import com.alertsystem.config.web.mapper.AlertRuleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/config/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService service;
    private final UserRepository userRepository;
    private final AlertRuleMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertRuleResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponseList(service.findAll())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertRuleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(service.findById(id))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<AlertRuleResponse>> create(
            @Valid @RequestBody AlertRuleRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        User creator = userRepository.findById(userId).orElseThrow();
        AlertRule rule = request.toEntity();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(service.create(rule, request.getDataSourceId(),
                        request.getNotificationChannelId(), creator))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<AlertRuleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AlertRuleRequest request) {
        AlertRule rule = request.toEntity();
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(
                service.update(id, rule, request.getDataSourceId(), request.getNotificationChannelId()))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
