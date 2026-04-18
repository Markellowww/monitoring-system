package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.NotificationChannel;
import com.alertsystem.config.web.dto.response.NotificationChannelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationChannelMapper {

    @Mapping(target = "type", expression = "java(ch.getType() != null ? ch.getType().name() : null)")
    NotificationChannelResponse toResponse(NotificationChannel ch);

    List<NotificationChannelResponse> toResponseList(List<NotificationChannel> list);
}
