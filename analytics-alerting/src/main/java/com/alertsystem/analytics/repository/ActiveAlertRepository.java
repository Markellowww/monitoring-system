package com.alertsystem.analytics.repository;

import com.alertsystem.analytics.entity.ActiveAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActiveAlertRepository extends JpaRepository<ActiveAlert, UUID> {
    Optional<ActiveAlert> findByRuleIdAndStatus(UUID ruleId, ActiveAlert.Status status);
    List<ActiveAlert> findAllByStatus(ActiveAlert.Status status);
    List<ActiveAlert> findAllByOrderByFiredAtDesc();
}
