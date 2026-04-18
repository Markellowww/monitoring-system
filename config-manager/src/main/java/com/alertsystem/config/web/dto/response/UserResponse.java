package com.alertsystem.config.web.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String role;
    private boolean active;
    private Instant createdAt;
}
