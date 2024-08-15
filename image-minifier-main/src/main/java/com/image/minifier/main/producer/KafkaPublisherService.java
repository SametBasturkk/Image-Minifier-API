package com.image.minifier.main.producer;

import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.util.ModelConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class KafkaPublisherService {

    private final KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate;
    private ModelConverter modelConverter;
    private final String topicName = "compression-topic";

    public KafkaPublisherService(KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate, ModelConverter modelConverter) {
        this.kafkaTemplate = kafkaTemplate;
        this.modelConverter = modelConverter;
    }

    public UUID publishCompressImageTopic(MultipartFile inputFile, Integer quality, String extension) throws IOException {
        if (inputFile == null || inputFile.isEmpty()) {
            throw new IllegalArgumentException("Input file cannot be null or empty");
        }
        UUID uuid = UUID.randomUUID();
        CompressImageTopicModel compressImageTopicModel = new CompressImageTopicModel(inputFile.getBytes(), quality, extension, uuid, null);
        kafkaTemplate.send(topicName, compressImageTopicModel);
        log.info("Published message: {}", compressImageTopicModel);
        return uuid;
    }
}
