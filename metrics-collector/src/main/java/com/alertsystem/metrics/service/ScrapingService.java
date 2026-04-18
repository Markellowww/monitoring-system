package com.alertsystem.metrics.service;

import com.alertsystem.common.events.MetricsRawEvent;
import com.alertsystem.metrics.kafka.MetricsProducer;
import com.alertsystem.metrics.model.DataSourceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingService {

    private final MetricsProducer metricsProducer;
    private final PrometheusParser prometheusParser;
    private final WebClient.Builder webClientBuilder;

    /** scrapeIntervalSec -> ScheduledFuture */
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public void addOrUpdate(DataSourceConfig config) {
        remove(config.getId().toString());

        if (!config.isActive() || !"HTTP_SCRAPE".equals(config.getType())) {
            return;
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> scrape(config),
                0,
                config.getScrapeIntervalSec(),
                TimeUnit.SECONDS
        );
        scheduledJobs.put(config.getId().toString(), future);
        log.info("Scheduled scraping for source: {} every {}s", config.getName(), config.getScrapeIntervalSec());
    }

    public void remove(String sourceId) {
        ScheduledFuture<?> future = scheduledJobs.remove(sourceId);
        if (future != null) {
            future.cancel(false);
            log.info("Removed scraping job for sourceId: {}", sourceId);
        }
    }

    private void scrape(DataSourceConfig config) {
        try {
            String responseBody = webClientBuilder.build()
                    .get()
                    .uri(config.getEndpointUrl())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                log.debug("Empty response from source: {}", config.getName());
                return;
            }

            List<MetricsRawEvent.MetricPoint> points = prometheusParser.parse(responseBody);

            if (points.isEmpty()) {
                return;
            }

            MetricsRawEvent event = MetricsRawEvent.builder()
                    .sourceId(config.getId())
                    .sourceName(config.getName())
                    .timestamp(Instant.now())
                    .metrics(points)
                    .build();

            metricsProducer.send(event);
            log.debug("Scraped {} metrics from {}", points.size(), config.getName());

        } catch (Exception e) {
            log.warn("Failed to scrape {}: {}", config.getName(), e.getMessage());
        }
    }

    public Map<String, Boolean> getJobStatuses() {
        Map<String, Boolean> statuses = new ConcurrentHashMap<>();
        scheduledJobs.forEach((id, future) ->
                statuses.put(id, !future.isCancelled() && !future.isDone()));
        return statuses;
    }
}
