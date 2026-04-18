package com.alertsystem.metrics.model;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DataSourceConfig {
    private UUID id;
    private String name;
    private String type;
    private String endpointUrl;
    private int scrapeIntervalSec;
    private Map<String, String> credentials;
    private boolean active;
}
