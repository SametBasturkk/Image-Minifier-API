package com.kafka.consumer.service;

import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.util.ModelConverter;
import com.kafka.consumer.compression.ImageCompressorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ImageListenerService {

    private final ImageCompressorService imageCompressorService;
    private final ImageCompressSender imageCompressSender;
    private final ModelConverter modelMapper;

    public ImageListenerService(ImageCompressorService imageCompressorService, ImageCompressSender imageCompressSender, ModelConverter modelMapper) {
        this.imageCompressorService = imageCompressorService;
        this.imageCompressSender = imageCompressSender;
        this.modelMapper = modelMapper;
    }

    @KafkaListener(topics = "compression-topic", groupId = "compression-group")
    public void consume(CompressImageTopicModel imageInfo) {
        imageCompressorService.compressImage(imageInfo.getInputFile(), imageInfo.getQuality(), imageInfo.getExtension(), imageInfo.getUuid());
        imageCompressSender.sendImageOk(imageInfo);

    }
}
