package com.kafka.consumer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import com.kafka.consumer.compression.ImageCompressorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class ImageListenerService {

    private final ImageCompressorService imageCompressorService;
    private final ImageCompressSender imageCompressSender;
    private final Executor executor;


    public ImageListenerService(ImageCompressorService imageCompressorService, ImageCompressSender imageCompressSender, Executor executor) {
        this.imageCompressorService = imageCompressorService;
        this.imageCompressSender = imageCompressSender;
        this.executor = executor;
    }

    @KafkaListener(topics = "compression-topic", groupId = "group_id")
    public void consume(CompressImageTopicModel imageInfo) {
        CompletableFuture<CompressImageTopicModel> compressionFuture = CompletableFuture.supplyAsync(() -> {
            imageCompressorService.compressImage(
                    imageInfo.getInputFile(),
                    imageInfo.getQuality(),
                    imageInfo.getExtension(),
                    imageInfo.getUuid()
            );
            log.info("Consumed message: {}", imageInfo);
            return imageInfo;
        }, executor);

        compressionFuture.thenAcceptAsync(compressedImage -> {
            try {
                imageCompressSender.sendImageOk(compressedImage);
                log.info("Sent compressed image: {}", compressedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
