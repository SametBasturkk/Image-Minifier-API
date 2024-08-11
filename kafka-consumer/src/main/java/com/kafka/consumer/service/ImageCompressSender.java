package com.kafka.consumer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.kafka.consumer.compression.ImageCompressorService.logger;

@Service
public class ImageCompressSender {

    private KafkaTemplate<String, String> kafkaTemplate;

    public ImageCompressSender(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    private String topicName = "compress-image-topic-resp";

    public void sendImageOk(CompressImageTopicModel compressImageTopicModel) {
        Path outputFile = Path.of("./compressed/" + compressImageTopicModel.getUuid() + "." + compressImageTopicModel.getExtension());
        byte[] compressedBase64Data = outputFile.toString().getBytes();

        CompressImageTopicModel compressImageTopicModelResp = new CompressImageTopicModel(null, compressImageTopicModel.getQuality(), compressImageTopicModel.getExtension(), compressImageTopicModel.getUuid(), compressedBase64Data);
        kafkaTemplate.send(topicName, compressImageTopicModelResp.toString());

        cleanFiles(outputFile, outputFile);
    }

    private void cleanFiles(Path uploadedFilePath, Path compressedFilePath) {
        try {
            Files.deleteIfExists(uploadedFilePath);
            Files.deleteIfExists(compressedFilePath);
            logger.info("Deleted files: {}, {}", uploadedFilePath, compressedFilePath);
        } catch (IOException e) {
            logger.error("Error deleting files: {}", e.getMessage());
        }
    }


}