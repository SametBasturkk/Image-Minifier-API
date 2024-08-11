package com.kafka.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@ComponentScan(basePackages = "com.ImageProcessor.util, com.kafka.consumer")
@EnableKafka
public class KafkaImageConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaImageConsumerApplication.class, args);
    }
}
