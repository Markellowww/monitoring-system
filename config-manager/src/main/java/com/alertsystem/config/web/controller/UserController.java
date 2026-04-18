package com.alertsystem.config.web.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.repository.UserRepository;
import com.alertsystem.config.web.dto.response.UserResponse;
import com.alertsystem.config.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/config/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(userRepository.findAll(pageable).map(mapper::toResponse)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(u))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable UUID id,
            @RequestParam User.Role role) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role);
            return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(userRepository.save(user))));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<ApiResponse<UserResponse>> setActive(
            @PathVariable UUID id,
            @RequestParam boolean active) {
        return userRepository.findById(id).map(user -> {
            user.setActive(active);
            return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(userRepository.save(user))));
        }).orElse(ResponseEntity.notFound().build());
    }
}
