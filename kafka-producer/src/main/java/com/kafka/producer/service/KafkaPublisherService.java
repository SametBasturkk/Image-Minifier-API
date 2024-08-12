package com.kafka.producer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class KafkaPublisherService {

    private final KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate;
    private final String topicName = "compression-topic";

    public KafkaPublisherService(KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public UUID publishCompressImageTopic(MultipartFile inputFile, Integer quality, String extension) throws IOException {
        if (inputFile == null || inputFile.isEmpty()) {
            throw new IllegalArgumentException("Input file cannot be null or empty");
        }
        UUID uuid = UUID.randomUUID();
        CompressImageTopicModel compressImageTopicModel = new CompressImageTopicModel(inputFile.getBytes(), quality, extension, uuid, null);
        kafkaTemplate.send(topicName, compressImageTopicModel);
        return uuid;
    }
}
