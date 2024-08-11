package com.kafka.producer.sender;

import com.ImageProcessor.model.CompressImageTopicModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.kafka.producer.compression.ImageCompressorService.logger;

@Service
public class ImageOkSender {

    private KafkaTemplate<String, String> kafkaTemplate;

    public ImageOkSender(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    private String topicName = "compress-image-topic-resp";

    public void sendImageOk(CompressImageTopicModel compressImageTopicModel) {
        Path outputFile = Path.of("./compressed/" + compressImageTopicModel.getUuid() + "." + compressImageTopicModel.getExtension());
        byte[] compressedBase64Data = outputFile.toString().getBytes();

        CompressImageTopicModel compressImageTopicModel = new CompressImageTopicModel(null, quality, extension, uuid, compressedBase64Data);
        kafkaTemplate.send(topicName, compressImageTopicModel.toString());

        cleanFiles(inputFile, outputFile);
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