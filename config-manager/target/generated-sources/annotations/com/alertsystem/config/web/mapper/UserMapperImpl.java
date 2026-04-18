package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.User;
import com.alertsystem.config.web.dto.response.UserResponse;
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
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User u) {
        if ( u == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setId( u.getId() );
        userResponse.setUsername( u.getUsername() );
        userResponse.setEmail( u.getEmail() );
        userResponse.setActive( u.isActive() );
        userResponse.setCreatedAt( u.getCreatedAt() );

        userResponse.setRole( u.getRole() != null ? u.getRole().name() : null );

        return userResponse;
    }

    @Override
    public List<UserResponse> toResponseList(List<User> list) {
        if ( list == null ) {
            return null;
        }

        List<UserResponse> list1 = new ArrayList<UserResponse>( list.size() );
        for ( User user : list ) {
            list1.add( toResponse( user ) );
        }

        return list1;
    }
}
