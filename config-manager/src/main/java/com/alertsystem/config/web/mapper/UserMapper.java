package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.User;
import com.alertsystem.config.web.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(u.getRole() != null ? u.getRole().name() : null)")
    UserResponse toResponse(User u);

    List<UserResponse> toResponseList(List<User> list);
}
