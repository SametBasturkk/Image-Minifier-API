package com.image.minifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories
@ComponentScan(basePackages = {
        "com.image.minifier",
        "com.ImageProcessor.util",
        "com.kafka.producer",
        "com.ImageProcessor.service",
        "com.ImageProcessor.repository"
})
public class ImageMinifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageMinifierApplication.class, args);
    }
}
