package com.kafka.consumer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.util.ModelConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;


@Slf4j
@Service
public class ImageCompressSender {

    private KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate;
    private ModelConverter modelConverter;

    public ImageCompressSender(KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate, ModelConverter modelConverter) {
        this.kafkaTemplate = kafkaTemplate;
        this.modelConverter = modelConverter;
    }


    private String topicName = "compress-image-topic-resp";

    public void sendImageOk(CompressImageTopicModel compressImageTopicModel) throws IOException {
        Path outputFile = Path.of("./compressed/" + compressImageTopicModel.getUuid());
        byte[] compressedBase64Data = Files.readAllBytes(outputFile);

        CompressImageTopicModel compressImageTopicModelResp = new CompressImageTopicModel(null, compressImageTopicModel.getQuality(), compressImageTopicModel.getExtension(), compressImageTopicModel.getUuid(), compressedBase64Data);
        kafkaTemplate.send(topicName, compressImageTopicModelResp);
        log.info("Published message: {}", compressImageTopicModelResp);

        cleanFiles(compressImageTopicModel.getUuid(), outputFile);
    }


    private void cleanFiles(UUID fileName, Path compressedFilePath) {
        try {
            Files.delete(Path.of("./uploads/" + fileName));
            Files.delete(Path.of("./compressed/" + fileName));
            log.info("Deleted files: {}, {}", fileName, compressedFilePath);
        } catch (IOException e) {
            log.error("Error deleting files: {}", e.getMessage());
        }
    }


}