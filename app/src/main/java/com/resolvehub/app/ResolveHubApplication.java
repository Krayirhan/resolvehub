package com.resolvehub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.resolvehub")
@EnableScheduling
public class ResolveHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResolveHubApplication.class, args);
    }
}
