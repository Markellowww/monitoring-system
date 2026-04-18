package com.alertsystem.config.repository;

import com.alertsystem.config.entity.GeneratedReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, UUID> {

    Page<GeneratedReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
