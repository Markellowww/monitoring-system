package com.alertsystem.analytics.repository;

import com.alertsystem.analytics.entity.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AlertHistoryRepository extends JpaRepository<AlertHistory, UUID> {
    List<AlertHistory> findAllByFiredAtBetweenOrderByFiredAtDesc(Instant from, Instant to);
    List<AlertHistory> findAllByRuleIdOrderByFiredAtDesc(UUID ruleId);
}
