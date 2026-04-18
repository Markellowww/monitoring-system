package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.ReportSchedule;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.web.dto.response.ReportScheduleResponse;
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
public class ReportScheduleMapperImpl implements ReportScheduleMapper {

    @Override
    public ReportScheduleResponse toResponse(ReportSchedule s) {
        if ( s == null ) {
            return null;
        }

        ReportScheduleResponse reportScheduleResponse = new ReportScheduleResponse();

        reportScheduleResponse.setId( s.getId() );
        reportScheduleResponse.setName( s.getName() );
        reportScheduleResponse.setCronExpr( s.getCronExpr() );
        reportScheduleResponse.setPeriodDays( s.getPeriodDays() );
        List<String> list = s.getRecipients();
        if ( list != null ) {
            reportScheduleResponse.setRecipients( new ArrayList<String>( list ) );
        }
        reportScheduleResponse.setActive( s.isActive() );
        reportScheduleResponse.setCreatedAt( s.getCreatedAt() );
        reportScheduleResponse.setUpdatedAt( s.getUpdatedAt() );
        reportScheduleResponse.setCreatedBy( userToUserRef( s.getCreatedBy() ) );

        reportScheduleResponse.setFormat( s.getFormat() != null ? s.getFormat().name() : null );

        return reportScheduleResponse;
    }

    @Override
    public List<ReportScheduleResponse> toResponseList(List<ReportSchedule> list) {
        if ( list == null ) {
            return null;
        }

        List<ReportScheduleResponse> list1 = new ArrayList<ReportScheduleResponse>( list.size() );
        for ( ReportSchedule reportSchedule : list ) {
            list1.add( toResponse( reportSchedule ) );
        }

        return list1;
    }

    protected ReportScheduleResponse.UserRef userToUserRef(User user) {
        if ( user == null ) {
            return null;
        }

        ReportScheduleResponse.UserRef userRef = new ReportScheduleResponse.UserRef();

        userRef.setId( user.getId() );
        userRef.setUsername( user.getUsername() );

        return userRef;
    }
}
