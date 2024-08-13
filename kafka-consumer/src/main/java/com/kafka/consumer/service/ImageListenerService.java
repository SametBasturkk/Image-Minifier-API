package com.kafka.consumer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.util.ModelConverter;
import com.kafka.consumer.compression.ImageCompressorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ImageListenerService {

    private final ImageCompressorService imageCompressorService;
    private final ImageCompressSender imageCompressSender;
    private final ModelConverter modelMapper;
    private final Executor executor;


    public ImageListenerService(ImageCompressorService imageCompressorService, ImageCompressSender imageCompressSender, ModelConverter modelMapper, Executor executor) {
        this.imageCompressorService = imageCompressorService;
        this.imageCompressSender = imageCompressSender;
        this.modelMapper = modelMapper;
        this.executor = executor;
    }

    @KafkaListener(topics = "compression-topic", groupId = "compression-group")
    public void consume(CompressImageTopicModel imageInfo) {
        CompletableFuture<CompressImageTopicModel> compressionFuture = CompletableFuture.supplyAsync(() -> {
            imageCompressorService.compressImage(
                    imageInfo.getInputFile(),
                    imageInfo.getQuality(),
                    imageInfo.getExtension(),
                    imageInfo.getUuid()
            );
            return imageInfo;
        }, executor);

        compressionFuture.thenAcceptAsync(compressedImage -> {
            imageCompressSender.sendImageOk(compressedImage);
        }, executor);
    }
}
