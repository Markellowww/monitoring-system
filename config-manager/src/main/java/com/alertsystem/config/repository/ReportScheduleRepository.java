package com.alertsystem.config.repository;

import com.alertsystem.config.entity.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID> {

}
