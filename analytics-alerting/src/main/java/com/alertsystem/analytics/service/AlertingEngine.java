package com.alertsystem.analytics.service;

import com.alertsystem.analytics.entity.ActiveAlert;
import com.alertsystem.analytics.entity.AlertHistory;
import com.alertsystem.analytics.kafka.AlertProducer;
import com.alertsystem.analytics.model.AlertRuleConfig;
import com.alertsystem.analytics.repository.ActiveAlertRepository;
import com.alertsystem.analytics.repository.AlertHistoryRepository;
import com.alertsystem.common.events.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertingEngine {

    private final ActiveAlertRepository activeAlertRepo;
    private final AlertHistoryRepository historyRepo;
    private final AlertProducer alertProducer;
    private final RuleEvaluator ruleEvaluator;
    private final ConfigSyncService configSyncService;
    private final WebClient.Builder webClientBuilder;

    @Value("${tsdb-storage.url}")
    private String tsdbUrl;

    /** Кэш правил: ruleId -> конфиг */
    private final Map<UUID, AlertRuleConfig> rules = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${alerting.evaluation-interval-sec:30}000")
    @Transactional
    public void evaluateAllRules() {
        rules.values().stream()
                .filter(AlertRuleConfig::isActive)
                .forEach(this::evaluateRule);
    }

    private void evaluateRule(AlertRuleConfig rule) {
        if (rule.getDataSource() == null) {
            return;
        }

        String sourceId = rule.getDataSource().getId().toString();

        try {
            Double avgValue = fetchAvg(rule.getMetricName(), sourceId, rule.getDurationSec());

            if (avgValue == null) {
                handleNoData(rule);
                return;
            }

            boolean conditionViolated = ruleEvaluator.evaluate(rule, avgValue);

            if (conditionViolated) {
                handleFiring(rule, avgValue);
            } else {
                handleResolved(rule, avgValue);
            }

        } catch (Exception e) {
            log.warn("Error evaluating rule {}: {}", rule.getName(), e.getMessage());
        }
    }

    private void handleFiring(AlertRuleConfig rule, double value) {
        activeAlertRepo.findByRuleIdAndStatus(rule.getId(), ActiveAlert.Status.FIRING)
                .ifPresentOrElse(
                        existing -> {
                            existing.setCurrentValue(value);
                            existing.setLastEvaluatedAt(Instant.now());
                            activeAlertRepo.save(existing);
                        },
                        () -> {
                            ActiveAlert alert = ActiveAlert.builder()
                                    .ruleId(rule.getId())
                                    .ruleName(rule.getName())
                                    .metricName(rule.getMetricName())
                                    .sourceName(rule.getDataSource().getName())
                                    .severity(ActiveAlert.Severity.valueOf(rule.getSeverity()))
                                    .status(ActiveAlert.Status.FIRING)
                                    .currentValue(value)
                                    .threshold(rule.getThreshold())
                                    .firedAt(Instant.now())
                                    .lastEvaluatedAt(Instant.now())
                                    .notificationChannelId(
                                            rule.getNotificationChannel() != null
                                                    ? rule.getNotificationChannel().getId() : null)
                                    .build();

                            activeAlertRepo.save(alert);

                            AlertEvent event = buildAlertEvent(alert, rule, AlertEvent.AlertStatus.FIRING);
                            alertProducer.send(event);

                            log.info("Alert FIRED: rule={}, value={}, threshold={}",
                                    rule.getName(), value, rule.getThreshold());
                        }
                );
    }

    private void handleResolved(AlertRuleConfig rule, double value) {
        activeAlertRepo.findByRuleIdAndStatus(rule.getId(), ActiveAlert.Status.FIRING)
                .ifPresent(alert -> {
                    alert.setStatus(ActiveAlert.Status.RESOLVED);
                    alert.setLastEvaluatedAt(Instant.now());
                    activeAlertRepo.save(alert);

                    AlertHistory history = AlertHistory.builder()
                            .ruleId(rule.getId())
                            .ruleName(rule.getName())
                            .severity(alert.getSeverity())
                            .firedAt(alert.getFiredAt())
                            .resolvedAt(Instant.now())
                            .maxValue(alert.getCurrentValue())
                            .sourceName(alert.getSourceName())
                            .build();
                    historyRepo.save(history);

                    AlertEvent event = buildAlertEvent(alert, rule, AlertEvent.AlertStatus.RESOLVED);
                    alertProducer.send(event);

                    log.info("Alert RESOLVED: rule={}", rule.getName());
                });
    }

    private void handleNoData(AlertRuleConfig rule) {
        if (!rule.isNoDataAlert()) {
            return;
        }
        // no-data alerting - если данных нет дольше noDataTimeoutSec, генерируем алерт
        activeAlertRepo.findByRuleIdAndStatus(rule.getId(), ActiveAlert.Status.FIRING)
                .ifPresentOrElse(
                        existing -> existing.setLastEvaluatedAt(Instant.now()),
                        () -> {
                            ActiveAlert alert = ActiveAlert.builder()
                                    .ruleId(rule.getId())
                                    .ruleName(rule.getName() + " [NO DATA]")
                                    .metricName(rule.getMetricName())
                                    .sourceName(rule.getDataSource().getName())
                                    .severity(ActiveAlert.Severity.WARNING)
                                    .status(ActiveAlert.Status.FIRING)
                                    .firedAt(Instant.now())
                                    .lastEvaluatedAt(Instant.now())
                                    .notificationChannelId(
                                            rule.getNotificationChannel() != null
                                                    ? rule.getNotificationChannel().getId() : null)
                                    .build();
                            activeAlertRepo.save(alert);
                            AlertEvent event = buildAlertEvent(alert, rule, AlertEvent.AlertStatus.FIRING);
                            alertProducer.send(event);
                            log.warn("No-data alert fired for rule: {}", rule.getName());
                        }
                );
    }

    private AlertEvent buildAlertEvent(ActiveAlert alert, AlertRuleConfig rule, AlertEvent.AlertStatus status) {
        return AlertEvent.builder()
                .id(alert.getId())
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .severity(AlertEvent.Severity.valueOf(rule.getSeverity()))
                .status(status)
                .metricName(rule.getMetricName())
                .currentValue(alert.getCurrentValue() != null ? alert.getCurrentValue() : 0)
                .threshold(rule.getThreshold())
                .sourceName(alert.getSourceName())
                .firedAt(alert.getFiredAt())
                .notificationChannelId(alert.getNotificationChannelId())
                .build();
    }

    private Double fetchAvg(String metricName, String sourceId, int seconds) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(tsdbUrl + "/api/storage/query/avg?metric={m}&sourceId={s}&seconds={sec}",
                            metricName, sourceId, seconds)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(resp -> {
                        Object data = resp.get("data");
                        return data instanceof Number n ? n.doubleValue() : null;
                    })
                    .block();
        } catch (Exception e) {
            log.debug("Failed to fetch avg for {}: {}", metricName, e.getMessage());
            return null;
        }
    }

    public void addOrUpdateRule(AlertRuleConfig rule) {
        rules.put(rule.getId(), rule);
    }

    public void removeRule(UUID ruleId) {
        rules.remove(ruleId);
    }

    public Map<UUID, AlertRuleConfig> getRules() {
        return rules;
    }
}
