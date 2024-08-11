package com.kafka.consumer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import com.kafka.consumer.compression.ImageCompressorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ImageListenerService {

    private ImageCompressorService imageCompressorService;
    private ImageCompressSender imageCompressSender;

    public ImageListenerService(ImageCompressorService imageCompressorService, ImageCompressSender imageCompressSender) {
        this.imageCompressorService = imageCompressorService;
        this.imageCompressSender = imageCompressSender;
    }

    @KafkaListener(topics = "compression-topic")
    public void listen(CompressImageTopicModel imageInfo) {
        imageCompressorService.compressImage(imageInfo.getInputFile(), imageInfo.getQuality(), imageInfo.getExtension(), imageInfo.getUuid());
        imageCompressSender.sendImageOk(imageInfo);

    }
}
