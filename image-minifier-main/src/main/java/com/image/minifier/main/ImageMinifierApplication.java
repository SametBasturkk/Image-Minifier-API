package com.image.minifier.main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@ComponentScan(basePackages = {
        "com.image.minifier.common",
        "com.kafka.producer",
        "com.image.minifier.main",
})
@SpringBootApplication
public class ImageMinifierApplication {
    public static void main(String[] args) {
        log.info("Starting Image Minifier Application");
        SpringApplication.run(ImageMinifierApplication.class, args);
    }
}
