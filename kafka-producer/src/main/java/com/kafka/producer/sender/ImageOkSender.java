package com.kafka.producer.sender;

import com.ImageProcessor.model.CompressImageTopicModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImageOkSender {

    private KafkaTemplate<String, String> kafkaTemplate;

    public ImageOkSender(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${spring.kafka.producer.topic}")
    private String topicName;

    public void sendImageOk(Path inputFile, Integer quality, String extension, UUID uuid) {
        CompressImageTopicModel compressImageTopicModel = new CompressImageTopicModel(inputFile, quality, extension, uuid);
        kafkaTemplate.send(topicName, compressImageTopicModel.toString());
    }


}