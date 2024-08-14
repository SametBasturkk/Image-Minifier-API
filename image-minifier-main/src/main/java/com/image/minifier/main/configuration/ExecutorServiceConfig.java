package com.image.minifier.main.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorServiceConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return
                Executors.newScheduledThreadPool(10);
    }
}
