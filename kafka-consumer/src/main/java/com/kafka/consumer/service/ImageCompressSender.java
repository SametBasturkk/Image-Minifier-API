package com.kafka.consumer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.util.ModelConverter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.kafka.consumer.compression.ImageCompressorService.logger;

@Service
public class ImageCompressSender {

    private KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate;
    private ModelConverter modelConverter;

    public ImageCompressSender(KafkaTemplate<String, CompressImageTopicModel> kafkaTemplate, ModelConverter modelConverter) {
        this.kafkaTemplate = kafkaTemplate;
        this.modelConverter = modelConverter;
    }


    private String topicName = "compress-image-topic-resp";

    public void sendImageOk(CompressImageTopicModel compressImageTopicModel) {
        Path outputFile = Path.of("./compressed/" + compressImageTopicModel.getUuid() + "." + compressImageTopicModel.getExtension());
        byte[] compressedBase64Data = outputFile.toString().getBytes();

        CompressImageTopicModel compressImageTopicModelResp = new CompressImageTopicModel(null, compressImageTopicModel.getQuality(), compressImageTopicModel.getExtension(), compressImageTopicModel.getUuid(), compressedBase64Data);
        kafkaTemplate.send(topicName, compressImageTopicModelResp);

        cleanFiles(compressImageTopicModel.getUuid(), outputFile);
    }

    private void cleanFiles(UUID fileName, Path compressedFilePath) {
        try {
            Files.deleteIfExists(Path.of("./uploads/" + fileName));
            Files.deleteIfExists(Path.of("./compressed/" + fileName));
            logger.info("Deleted files: {}, {}", fileName, compressedFilePath);
        } catch (IOException e) {
            logger.error("Error deleting files: {}", e.getMessage());
        }
    }


}