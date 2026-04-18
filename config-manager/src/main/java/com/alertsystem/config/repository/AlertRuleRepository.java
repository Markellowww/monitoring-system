package com.alertsystem.config.repository;

import com.alertsystem.config.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {

}
