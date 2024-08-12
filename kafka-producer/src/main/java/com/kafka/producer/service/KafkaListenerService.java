package com.kafka.producer.service;


import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.model.ImageStatus;
import com.image.minifier.common.service.ImageStatusService;


public class KafkaListenerService {

    private ImageStatusService imageStatusService;

    public KafkaListenerService(ImageStatusService imageStatusService) {
        this.imageStatusService = imageStatusService;
    }


    public void consume(CompressImageTopicModel message) {
        ImageStatus resp = new ImageStatus();
        resp.setCompressed(true);
        resp.setCompressedBase64Data(message.getCompressedBase64Data());
        imageStatusService.saveImageStatus(resp);
    }
}
