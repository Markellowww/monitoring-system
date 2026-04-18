package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.ReportSchedule;
import com.alertsystem.config.web.dto.response.ReportScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportScheduleMapper {

    @Mapping(target = "format", expression = "java(s.getFormat() != null ? s.getFormat().name() : null)")
    ReportScheduleResponse toResponse(ReportSchedule s);

    List<ReportScheduleResponse> toResponseList(List<ReportSchedule> list);
}
