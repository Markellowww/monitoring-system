package com.alertsystem.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisualReportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisualReportsApplication.class, args);
    }
}
