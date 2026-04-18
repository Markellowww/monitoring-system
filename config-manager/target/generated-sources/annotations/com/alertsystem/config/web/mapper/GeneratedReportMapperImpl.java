package com.alertsystem.config.web.mapper;

import com.alertsystem.config.entity.GeneratedReport;
import com.alertsystem.config.entity.ReportSchedule;
import com.alertsystem.config.entity.User;
import com.alertsystem.config.web.dto.response.GeneratedReportResponse;
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
public class GeneratedReportMapperImpl implements GeneratedReportMapper {

    @Override
    public GeneratedReportResponse toResponse(GeneratedReport r) {
        if ( r == null ) {
            return null;
        }

        GeneratedReportResponse generatedReportResponse = new GeneratedReportResponse();

        generatedReportResponse.setId( r.getId() );
        generatedReportResponse.setSchedule( reportScheduleToScheduleRef( r.getSchedule() ) );
        generatedReportResponse.setPeriodFrom( r.getPeriodFrom() );
        generatedReportResponse.setPeriodTo( r.getPeriodTo() );
        generatedReportResponse.setFilePath( r.getFilePath() );
        generatedReportResponse.setCreatedAt( r.getCreatedAt() );
        generatedReportResponse.setCreatedBy( userToUserRef( r.getCreatedBy() ) );

        generatedReportResponse.setFormat( r.getFormat() != null ? r.getFormat().name() : null );

        return generatedReportResponse;
    }

    @Override
    public List<GeneratedReportResponse> toResponseList(List<GeneratedReport> list) {
        if ( list == null ) {
            return null;
        }

        List<GeneratedReportResponse> list1 = new ArrayList<GeneratedReportResponse>( list.size() );
        for ( GeneratedReport generatedReport : list ) {
            list1.add( toResponse( generatedReport ) );
        }

        return list1;
    }

    protected GeneratedReportResponse.ScheduleRef reportScheduleToScheduleRef(ReportSchedule reportSchedule) {
        if ( reportSchedule == null ) {
            return null;
        }

        GeneratedReportResponse.ScheduleRef scheduleRef = new GeneratedReportResponse.ScheduleRef();

        scheduleRef.setId( reportSchedule.getId() );
        scheduleRef.setName( reportSchedule.getName() );

        return scheduleRef;
    }

    protected GeneratedReportResponse.UserRef userToUserRef(User user) {
        if ( user == null ) {
            return null;
        }

        GeneratedReportResponse.UserRef userRef = new GeneratedReportResponse.UserRef();

        userRef.setId( user.getId() );
        userRef.setUsername( user.getUsername() );

        return userRef;
    }
}
