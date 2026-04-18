package com.alertsystem.analytics.config;

import com.alertsystem.analytics.service.AlertingEngine;
import com.alertsystem.analytics.service.ConfigSyncService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    /**
     * Разрываем циклическую зависимость ConfigSyncService <-> AlertingEngine
     * через явную инициализацию после создания обоих бинов.
     */
    @Bean
    public String configSyncInit(ConfigSyncService syncService, AlertingEngine engine) {
        syncService.setAlertingEngine(engine);
        syncService.loadInitialRules();
        return "initialized";
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
