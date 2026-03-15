package com.executor.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.executor.worker")
@EntityScan(basePackages = "com.executor.common")
public class WorkerApp {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApp.class, args);
    }
}