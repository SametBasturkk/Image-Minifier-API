package com.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.image.minifier.common, com.kafka.consumer")
@EnableKafka
public class KafkaImageConsumerApplication {
    public static void main(String[] args) {
        log.info("Starting Kafka Image Consumer Application");
        SpringApplication.run(KafkaImageConsumerApplication.class, args);
    }
}
