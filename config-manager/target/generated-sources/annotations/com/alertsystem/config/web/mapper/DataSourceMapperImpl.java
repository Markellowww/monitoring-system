package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.DataSource;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.web.dto.response.DataSourceResponse;
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
public class DataSourceMapperImpl implements DataSourceMapper {

    @Override
    public DataSourceResponse toResponse(DataSource ds) {
        if ( ds == null ) {
            return null;
        }

        DataSourceResponse dataSourceResponse = new DataSourceResponse();

        dataSourceResponse.setId( ds.getId() );
        dataSourceResponse.setName( ds.getName() );
        dataSourceResponse.setEndpointUrl( ds.getEndpointUrl() );
        dataSourceResponse.setScrapeIntervalSec( ds.getScrapeIntervalSec() );
        Map<String, String> map = ds.getCredentials();
        if ( map != null ) {
            dataSourceResponse.setCredentials( new LinkedHashMap<String, String>( map ) );
        }
        dataSourceResponse.setActive( ds.isActive() );
        dataSourceResponse.setCreatedAt( ds.getCreatedAt() );
        dataSourceResponse.setUpdatedAt( ds.getUpdatedAt() );
        dataSourceResponse.setCreatedBy( userToUserRef( ds.getCreatedBy() ) );

        dataSourceResponse.setType( ds.getType() != null ? ds.getType().name() : null );

        return dataSourceResponse;
    }

    @Override
    public List<DataSourceResponse> toResponseList(List<DataSource> list) {
        if ( list == null ) {
            return null;
        }

        List<DataSourceResponse> list1 = new ArrayList<DataSourceResponse>( list.size() );
        for ( DataSource dataSource : list ) {
            list1.add( toResponse( dataSource ) );
        }

        return list1;
    }

    protected DataSourceResponse.UserRef userToUserRef(User user) {
        if ( user == null ) {
            return null;
        }

        DataSourceResponse.UserRef userRef = new DataSourceResponse.UserRef();

        userRef.setId( user.getId() );
        userRef.setUsername( user.getUsername() );

        return userRef;
    }
}
