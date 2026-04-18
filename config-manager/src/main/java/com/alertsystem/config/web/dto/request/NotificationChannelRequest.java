package com.alertsystem.config.web.dto.request;

import com.alertsystem.config.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationChannelRequest {

    @NotBlank
    private String name;
    @NotNull
    private NotificationChannel.Type type;
    @NotNull
    private Map<String, Object> config;

    public NotificationChannel toEntity() {
        return NotificationChannel.builder()
                .name(name)
                .type(type)
                .config(config)
                .build();
    }
}
