package com.alertsystem.config.web.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.config.entity.NotificationChannel;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.repository.NotificationChannelRepository;
import com.alertsystem.config.repository.UserRepository;
import com.alertsystem.config.web.dto.request.NotificationChannelRequest;
import com.alertsystem.config.web.dto.response.NotificationChannelResponse;
import com.alertsystem.config.web.mapper.NotificationChannelMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/config/notification-channels")
@RequiredArgsConstructor
public class NotificationChannelController {

    private final NotificationChannelRepository repository;
    private final UserRepository userRepository;
    private final NotificationChannelMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationChannelResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponseList(repository.findAll())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationChannelResponse>> getById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ch -> ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(ch))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<NotificationChannelResponse>> create(
            @Valid @RequestBody NotificationChannelRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        User creator = userRepository.findById(userId).orElseThrow();
        NotificationChannel channel = request.toEntity();
        channel.setCreatedBy(creator);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(repository.save(channel))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEAD_ENGINEER')")
    public ResponseEntity<ApiResponse<NotificationChannelResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationChannelRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setType(request.getType());
            existing.setConfig(request.getConfig());
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
