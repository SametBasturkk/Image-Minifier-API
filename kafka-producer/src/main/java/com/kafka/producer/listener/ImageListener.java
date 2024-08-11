package com.kafka.producer.listener;

import com.ImageProcessor.model.CompressImageTopicModel;
import com.kafka.producer.compression.ImageCompressorService;
import com.kafka.producer.sender.ImageOkSender;
import org.springframework.kafka.annotation.KafkaListener;

public class ImageListener {

    private ImageCompressorService imageCompressorService;
    private ImageOkSender imageOkSender;

    public ImageListener(ImageCompressorService imageCompressorService, ImageOkSender imageOkSender) {
        this.imageCompressorService = imageCompressorService;
        this.imageOkSender = imageOkSender;
    }

    @KafkaListener(topics = "compression-topic")
    public void listen(CompressImageTopicModel imageInfo) {
        imageCompressorService.compressImage(imageInfo.getInputFile(), imageInfo.getQuality(), imageInfo.getExtension(), imageInfo.getUuid());
        imageOkSender.sendImageOk(imageInfo);

    }
}
