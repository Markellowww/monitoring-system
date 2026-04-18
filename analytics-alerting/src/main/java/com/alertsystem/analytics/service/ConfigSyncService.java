package com.alertsystem.analytics.service;

import com.alertsystem.analytics.model.AlertRuleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigSyncService {

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    @Value("${config-manager.url}")
    private String configManagerUrl;

    // Circular dependency breaking - AlertingEngine инжектируется через сеттер
    private AlertingEngine alertingEngine;

    public void setAlertingEngine(AlertingEngine engine) {
        this.alertingEngine = engine;
    }

    public void loadInitialRules() {
        try {
            List<AlertRuleConfig> ruleList = webClientBuilder.build()
                    .get()
                    .uri(configManagerUrl + "/api/config/alert-rules")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(resp -> {
                        Object data = resp.get("data");
                        return (List<AlertRuleConfig>) objectMapper.convertValue(data,
                                objectMapper.getTypeFactory()
                                        .constructCollectionType(List.class, AlertRuleConfig.class));
                    })
                    .block();

            if (ruleList != null && alertingEngine != null) {
                ruleList.forEach(alertingEngine::addOrUpdateRule);
                log.info("Loaded {} alert rules from config-manager", ruleList.size());
            }
        } catch (Exception e) {
            log.warn("Could not load initial alert rules: {}", e.getMessage());
        }
    }

    public void handleRuleUpdate(String payload) {
        try {
            AlertRuleConfig rule = objectMapper.readValue(payload, AlertRuleConfig.class);
            if (alertingEngine != null) {
                alertingEngine.addOrUpdateRule(rule);
            }
        } catch (Exception e) {
            log.error("Failed to parse alert rule update: {}", e.getMessage());
        }
    }

    public void handleRuleDelete(String ruleId) {
        if (alertingEngine != null) {
            alertingEngine.removeRule(UUID.fromString(ruleId));
        }
    }
}
