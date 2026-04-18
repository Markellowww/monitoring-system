package com.alertsystem.config.web.dto.request;

import com.alertsystem.config.entity.DataSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class DataSourceRequest {

    @NotBlank
    private String name;
    @NotNull
    private DataSource.Type type;
    private String endpointUrl;
    private int scrapeIntervalSec = 30;
    private Map<String, String> credentials;
    private boolean active = true;

    public DataSource toEntity() {
        return DataSource.builder()
                .name(name)
                .type(type)
                .endpointUrl(endpointUrl)
                .scrapeIntervalSec(scrapeIntervalSec)
                .credentials(credentials)
                .active(active)
                .build();
    }
}
