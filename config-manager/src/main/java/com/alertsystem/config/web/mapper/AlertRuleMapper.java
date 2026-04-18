package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.AlertRule;
import com.alertsystem.config.entity.DataSource;
import com.alertsystem.config.entity.NotificationChannel;
import com.alertsystem.config.web.dto.response.AlertRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlertRuleMapper {

    @Mapping(target = "severity", expression = "java(rule.getSeverity() != null ? rule.getSeverity().name() : null)")
    AlertRuleResponse toResponse(AlertRule rule);

    List<AlertRuleResponse> toResponseList(List<AlertRule> rules);

    @Mapping(target = "type", expression = "java(ds.getType() != null ? ds.getType().name() : null)")
    AlertRuleResponse.DataSourceRef toDataSourceRef(DataSource ds);

    @Mapping(target = "type", expression = "java(ch.getType() != null ? ch.getType().name() : null)")
    AlertRuleResponse.NotificationChannelRef toNotificationChannelRef(NotificationChannel ch);
}
