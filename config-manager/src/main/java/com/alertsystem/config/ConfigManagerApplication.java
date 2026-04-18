package com.alertsystem.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConfigManagerApplication {

    public static void main(String[] args) {

        SpringApplication.run(ConfigManagerApplication.class, args);
    }
}
