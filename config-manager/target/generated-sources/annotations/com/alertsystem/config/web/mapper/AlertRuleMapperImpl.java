package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.AlertRule;
import com.alertsystem.config.entity.DataSource;
import com.alertsystem.config.entity.NotificationChannel;
import com.alertsystem.config.web.dto.response.AlertRuleResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-15T23:49:17+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class AlertRuleMapperImpl implements AlertRuleMapper {

    @Override
    public AlertRuleResponse toResponse(AlertRule rule) {
        if ( rule == null ) {
            return null;
        }

        AlertRuleResponse alertRuleResponse = new AlertRuleResponse();

        alertRuleResponse.setId( rule.getId() );
        alertRuleResponse.setName( rule.getName() );
        alertRuleResponse.setMetricName( rule.getMetricName() );
        alertRuleResponse.setCondition( rule.getCondition() );
        alertRuleResponse.setThreshold( rule.getThreshold() );
        alertRuleResponse.setDurationSec( rule.getDurationSec() );
        alertRuleResponse.setNoDataAlert( rule.isNoDataAlert() );
        alertRuleResponse.setNoDataTimeoutSec( rule.getNoDataTimeoutSec() );
        alertRuleResponse.setActive( rule.isActive() );
        alertRuleResponse.setCreatedAt( rule.getCreatedAt() );
        alertRuleResponse.setUpdatedAt( rule.getUpdatedAt() );
        alertRuleResponse.setDataSource( toDataSourceRef( rule.getDataSource() ) );
        alertRuleResponse.setNotificationChannel( toNotificationChannelRef( rule.getNotificationChannel() ) );

        alertRuleResponse.setSeverity( rule.getSeverity() != null ? rule.getSeverity().name() : null );

        return alertRuleResponse;
    }

    @Override
    public List<AlertRuleResponse> toResponseList(List<AlertRule> rules) {
        if ( rules == null ) {
            return null;
        }

        List<AlertRuleResponse> list = new ArrayList<AlertRuleResponse>( rules.size() );
        for ( AlertRule alertRule : rules ) {
            list.add( toResponse( alertRule ) );
        }

        return list;
    }

    @Override
    public AlertRuleResponse.DataSourceRef toDataSourceRef(DataSource ds) {
        if ( ds == null ) {
            return null;
        }

        AlertRuleResponse.DataSourceRef dataSourceRef = new AlertRuleResponse.DataSourceRef();

        dataSourceRef.setId( ds.getId() );
        dataSourceRef.setName( ds.getName() );

        dataSourceRef.setType( ds.getType() != null ? ds.getType().name() : null );

        return dataSourceRef;
    }

    @Override
    public AlertRuleResponse.NotificationChannelRef toNotificationChannelRef(NotificationChannel ch) {
        if ( ch == null ) {
            return null;
        }

        AlertRuleResponse.NotificationChannelRef notificationChannelRef = new AlertRuleResponse.NotificationChannelRef();

        notificationChannelRef.setId( ch.getId() );
        notificationChannelRef.setName( ch.getName() );

        notificationChannelRef.setType( ch.getType() != null ? ch.getType().name() : null );

        return notificationChannelRef;
    }
}
