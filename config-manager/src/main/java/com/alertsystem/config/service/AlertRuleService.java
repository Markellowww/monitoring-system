package com.alertsystem.config.service;

import com.alertsystem.common.events.ConfigUpdatedEvent;
import com.alertsystem.config.entity.AlertRule;
import com.alertsystem.config.entity.DataSource;
import com.alertsystem.config.entity.NotificationChannel;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.kafka.ConfigEventProducer;
import com.alertsystem.config.repository.AlertRuleRepository;
import com.alertsystem.config.repository.DataSourceRepository;
import com.alertsystem.config.repository.NotificationChannelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertRuleService {

    private final AlertRuleRepository repository;
    private final DataSourceRepository dataSourceRepository;
    private final NotificationChannelRepository channelRepository;
    private final ConfigEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    public List<AlertRule> findAll() {
        return repository.findAll();
    }

    public AlertRule findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AlertRule not found: " + id));
    }

    @Transactional
    @SneakyThrows
    public AlertRule create(AlertRule rule, UUID dataSourceId, UUID channelId, User creator) {
        if (dataSourceId != null) {
            DataSource ds = dataSourceRepository.findById(dataSourceId)
                    .orElseThrow(() -> new IllegalArgumentException("DataSource not found: " + dataSourceId));
            rule.setDataSource(ds);
        }
        if (channelId != null) {
            NotificationChannel ch = channelRepository.findById(channelId)
                    .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));
            rule.setNotificationChannel(ch);
        }
        rule.setCreatedBy(creator);
        AlertRule saved = repository.save(rule);
        eventProducer.publish(ConfigUpdatedEvent.builder()
                .targetService("analytics-alerting")
                .type(ConfigUpdatedEvent.EventType.ALERT_RULE_CREATED)
                .entityId(saved.getId().toString())
                .payload(objectMapper.writeValueAsString(saved))
                .build());
        return saved;
    }

    @Transactional
    @SneakyThrows
    public AlertRule update(UUID id, AlertRule updated, UUID dataSourceId, UUID channelId) {
        AlertRule existing = findById(id);
        existing.setName(updated.getName());
        existing.setMetricName(updated.getMetricName());
        existing.setCondition(updated.getCondition());
        existing.setThreshold(updated.getThreshold());
        existing.setDurationSec(updated.getDurationSec());
        existing.setSeverity(updated.getSeverity());
        existing.setNoDataAlert(updated.isNoDataAlert());
        existing.setNoDataTimeoutSec(updated.getNoDataTimeoutSec());
        existing.setActive(updated.isActive());

        if (dataSourceId != null) {
            existing.setDataSource(dataSourceRepository.findById(dataSourceId).orElse(null));
        }
        if (channelId != null) {
            existing.setNotificationChannel(channelRepository.findById(channelId).orElse(null));
        }

        AlertRule saved = repository.save(existing);
        eventProducer.publish(ConfigUpdatedEvent.builder()
                .targetService("analytics-alerting")
                .type(ConfigUpdatedEvent.EventType.ALERT_RULE_UPDATED)
                .entityId(id.toString())
                .payload(objectMapper.writeValueAsString(saved))
                .build());
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
        eventProducer.publish(ConfigUpdatedEvent.builder()
                .targetService("analytics-alerting")
                .type(ConfigUpdatedEvent.EventType.ALERT_RULE_DELETED)
                .entityId(id.toString())
                .payload("{}")
                .build());
    }
}
