package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.NotificationChannel;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.web.dto.response.NotificationChannelResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-15T23:49:17+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class NotificationChannelMapperImpl implements NotificationChannelMapper {

    @Override
    public NotificationChannelResponse toResponse(NotificationChannel ch) {
        if ( ch == null ) {
            return null;
        }

        NotificationChannelResponse notificationChannelResponse = new NotificationChannelResponse();

        notificationChannelResponse.setId( ch.getId() );
        notificationChannelResponse.setName( ch.getName() );
        Map<String, Object> map = ch.getConfig();
        if ( map != null ) {
            notificationChannelResponse.setConfig( new LinkedHashMap<String, Object>( map ) );
        }
        notificationChannelResponse.setCreatedAt( ch.getCreatedAt() );
        notificationChannelResponse.setUpdatedAt( ch.getUpdatedAt() );
        notificationChannelResponse.setCreatedBy( userToUserRef( ch.getCreatedBy() ) );

        notificationChannelResponse.setType( ch.getType() != null ? ch.getType().name() : null );

        return notificationChannelResponse;
    }

    @Override
    public List<NotificationChannelResponse> toResponseList(List<NotificationChannel> list) {
        if ( list == null ) {
            return null;
        }

        List<NotificationChannelResponse> list1 = new ArrayList<NotificationChannelResponse>( list.size() );
        for ( NotificationChannel notificationChannel : list ) {
            list1.add( toResponse( notificationChannel ) );
        }

        return list1;
    }

    protected NotificationChannelResponse.UserRef userToUserRef(User user) {
        if ( user == null ) {
            return null;
        }

        NotificationChannelResponse.UserRef userRef = new NotificationChannelResponse.UserRef();

        userRef.setId( user.getId() );
        userRef.setUsername( user.getUsername() );

        return userRef;
    }
}
