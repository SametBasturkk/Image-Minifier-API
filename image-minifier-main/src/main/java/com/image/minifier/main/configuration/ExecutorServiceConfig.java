package com.image.minifier.main.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorServiceConfig {

    @Value("${executor.core.pool.size}")
    private int corePoolSize;

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return
                Executors.newScheduledThreadPool(corePoolSize);
    }
}
