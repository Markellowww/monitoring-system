package com.alertsystem.config.web.controller;

import com.alertsystem.common.dto.ApiResponse;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.repository.UserRepository;
import com.alertsystem.config.service.AuthService;
import com.alertsystem.config.web.dto.request.LoginRequest;
import com.alertsystem.config.web.dto.request.RegisterRequest;
import com.alertsystem.config.web.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> me(
            @RequestHeader("X-User-Id") UUID userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(ApiResponse.ok(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
