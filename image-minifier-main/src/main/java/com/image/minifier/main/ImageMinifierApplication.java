package com.image.minifier.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@ComponentScan(basePackages = {
        "com.image.minifier.common",
        "com.kafka.producer",
        "com.image.minifier.main",
})
@Configuration
@EnableAutoConfiguration
@EnableRedisRepositories
@EnableJpaRepositories
public class ImageMinifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageMinifierApplication.class, args);
    }
}
