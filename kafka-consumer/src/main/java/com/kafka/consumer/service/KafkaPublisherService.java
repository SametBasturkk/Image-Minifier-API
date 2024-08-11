package com.kafka.consumer.service;

import com.ImageProcessor.model.CompressImageTopicModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

@Service
public class KafkaPublisherService {

    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaPublisherService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    private String topicName = "compression-topic";

    public UUID publishCompressImageTopic(Path inputFile, Integer quality, String extension) {
        UUID uuid = UUID.randomUUID();
        CompressImageTopicModel compressImageTopicModel = new CompressImageTopicModel(inputFile, quality, extension, uuid);
        kafkaTemplate.send(topicName, compressImageTopicModel.toString());
        return uuid;
    }


}