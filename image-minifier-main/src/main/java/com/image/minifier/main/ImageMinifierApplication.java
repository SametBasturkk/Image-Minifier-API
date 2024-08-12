package com.image.minifier.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
        "com.image.minifier.common",
        "com.kafka.producer",
        "com.image.minifier.main",
})
@SpringBootApplication
public class ImageMinifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageMinifierApplication.class, args);
    }
}
