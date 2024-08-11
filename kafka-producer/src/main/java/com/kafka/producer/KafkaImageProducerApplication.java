package com.kafka.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.ImageProcessor.util")
public class KafkaImageProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaImageProducerApplication.class, args);
    }
}
