package com.fintoc.logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class FintocApiLoggerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintocApiLoggerApplication.class, args);
    }
}