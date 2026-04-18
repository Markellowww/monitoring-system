package com.alertsystem.config.web.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.repository.UserRepository;
import com.alertsystem.config.service.DataSourceService;
import com.alertsystem.config.web.dto.request.DataSourceRequest;
import com.alertsystem.config.web.dto.response.DataSourceResponse;
import com.alertsystem.config.web.mapper.DataSourceMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/config/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService service;
    private final UserRepository userRepository;
    private final DataSourceMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DataSourceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponseList(service.findAll())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DataSourceResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(service.findById(id))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<DataSourceResponse>> create(
            @Valid @RequestBody DataSourceRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        User creator = userRepository.findById(userId).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(service.create(request.toEntity(), creator))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<DataSourceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DataSourceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(service.update(id, request.toEntity()))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
