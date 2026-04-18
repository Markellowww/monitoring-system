package com.alertsystem.config.service;

import com.alertsystem.common.events.ConfigUpdatedEvent;
import com.alertsystem.config.entity.DataSource;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.kafka.ConfigEventProducer;
import com.alertsystem.config.repository.DataSourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataSourceService {

    private final DataSourceRepository repository;
    private final ConfigEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    public List<DataSource> findAll() {
        return repository.findAll();
    }

    public DataSource findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DataSource not found: " + id));
    }

    @Transactional
    @SneakyThrows
    public DataSource create(DataSource source, User creator) {
        source.setCreatedBy(creator);
        DataSource saved = repository.save(source);
        eventProducer.publish(ConfigUpdatedEvent.builder()
                .targetService("metrics-collector")
                .type(ConfigUpdatedEvent.EventType.DATA_SOURCE_CREATED)
                .entityId(saved.getId().toString())
                .payload(objectMapper.writeValueAsString(saved))
                .build());
        return saved;
    }

    @Transactional
    @SneakyThrows
    public DataSource update(UUID id, DataSource updated) {
        DataSource existing = findById(id);
        existing.setName(updated.getName());
        existing.setEndpointUrl(updated.getEndpointUrl());
        existing.setScrapeIntervalSec(updated.getScrapeIntervalSec());
        existing.setCredentials(updated.getCredentials());
        existing.setActive(updated.isActive());
        DataSource saved = repository.save(existing);
        eventProducer.publish(ConfigUpdatedEvent.builder()
                .targetService("metrics-collector")
                .type(ConfigUpdatedEvent.EventType.DATA_SOURCE_UPDATED)
                .entityId(id.toString())
                .payload(objectMapper.writeValueAsString(saved))
                .build());
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
        eventProducer.publish(ConfigUpdatedEvent.builder()
                .targetService("metrics-collector")
                .type(ConfigUpdatedEvent.EventType.DATA_SOURCE_DELETED)
                .entityId(id.toString())
                .payload("{}")
                .build());
    }
}
