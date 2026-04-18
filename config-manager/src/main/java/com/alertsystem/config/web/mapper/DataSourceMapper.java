package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.DataSource;
import com.alertsystem.config.web.dto.response.DataSourceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DataSourceMapper {

    @Mapping(target = "type", expression = "java(ds.getType() != null ? ds.getType().name() : null)")
    DataSourceResponse toResponse(DataSource ds);

    List<DataSourceResponse> toResponseList(List<DataSource> list);
}
