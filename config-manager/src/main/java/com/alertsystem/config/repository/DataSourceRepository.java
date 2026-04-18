package com.alertsystem.config.repository;

import com.alertsystem.config.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {

}
