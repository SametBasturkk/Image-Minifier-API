package com.kafka.producer.listener;

import com.ImageProcessor.model.CompressImageTopicModel;
import com.kafka.producer.compression.ImageCompressorService;
import com.kafka.producer.sender.ImageOkSender;
import org.springframework.kafka.annotation.KafkaListener;

@KafkaListener(topics = "compression-topic", groupId = "group_id")
public class ImageListener {

    private ImageCompressorService imageCompressorService;
    private ImageOkSender imageOkSender;

    public ImageListener(ImageCompressorService imageCompressorService, ImageOkSender imageOkSender) {
        this.imageCompressorService = imageCompressorService;
        this.imageOkSender = imageOkSender;
    }

    public void consume(CompressImageTopicModel imageInfo) {
        imageCompressorService.compressImage(imageInfo.getInputFile(), imageInfo.getQuality(), imageInfo.getExtension());
        imageOkSender.sendImageOk(imageInfo.getInputFile(), imageInfo.getQuality(), imageInfo.getExtension(), imageInfo.getUuid());


    }
}
