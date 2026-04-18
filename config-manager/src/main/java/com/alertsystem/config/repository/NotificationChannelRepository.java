package com.alertsystem.config.repository;

import com.alertsystem.config.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, UUID> {

}
