package com.kafka.consumer.service;

import com.ImageProcessor.model.CompressImageTopicModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class KafkaPublisherService {

    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaPublisherService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    private String topicName = "compression-topic";

    public UUID publishCompressImageTopic(MultipartFile inputFile, Integer quality, String extension) throws IOException {
        UUID uuid = UUID.randomUUID();
        CompressImageTopicModel compressImageTopicModel = new CompressImageTopicModel(inputFile.getBytes(), quality, extension, uuid, null);
        kafkaTemplate.send(topicName, compressImageTopicModel.toString());
        return uuid;
    }


}