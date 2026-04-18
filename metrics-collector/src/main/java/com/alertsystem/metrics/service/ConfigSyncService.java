package com.alertsystem.metrics.service;

import com.alertsystem.metrics.model.DataSourceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigSyncService {

    private final ScrapingService scrapingService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${config-manager.url}")
    private String configManagerUrl;

    @PostConstruct
    public void loadInitialConfig() {
        try {
            List<DataSourceConfig> sources = webClientBuilder.build()
                    .get()
                    .uri(configManagerUrl + "/api/config/data-sources")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .map(resp -> {
                        Object data = resp.get("data");
                        return (List<DataSourceConfig>) objectMapper.convertValue(data,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, DataSourceConfig.class));
                    })
                    .block();

            if (sources != null) {
                sources.forEach(scrapingService::addOrUpdate);
                log.info("Loaded {} data sources from config-manager", sources.size());
            }
        } catch (Exception e) {
            log.warn("Could not load initial config from config-manager: {}", e.getMessage());
        }
    }

    public void handleDataSourceUpdate(String payload) {
        try {
            DataSourceConfig config = objectMapper.readValue(payload, DataSourceConfig.class);
            scrapingService.addOrUpdate(config);
        } catch (Exception e) {
            log.error("Failed to handle data source update: {}", e.getMessage());
        }
    }

    public void handleDataSourceDelete(String sourceId) {
        scrapingService.remove(sourceId);
    }
}
