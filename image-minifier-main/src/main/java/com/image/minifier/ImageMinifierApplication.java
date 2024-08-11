package com.image.minifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.image.minifier", "com.ImageProcessor.util", "com.kafka.consumer.service"})
public class ImageMinifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageMinifierApplication.class, args);
    }
}
