package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.GeneratedReport;
import com.alertsystem.config.web.dto.response.GeneratedReportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GeneratedReportMapper {

    @Mapping(target = "format", expression = "java(r.getFormat() != null ? r.getFormat().name() : null)")
    GeneratedReportResponse toResponse(GeneratedReport r);

    List<GeneratedReportResponse> toResponseList(List<GeneratedReport> list);
}
