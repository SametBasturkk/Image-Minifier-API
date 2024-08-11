package com.kafka.producer.service;


import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.model.ImageStatus;
import com.image.minifier.common.service.ImageStatusService;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@EnableKafka
@Service
public class KafkaListenerService {

    private ImageStatusService imageStatusService;

    public KafkaListenerService(ImageStatusService imageStatusService) {
        this.imageStatusService = imageStatusService;
    }


    @KafkaListener(topics = "compress-image-topic-resp", groupId = "group_id")
    public void consume(CompressImageTopicModel message) {
        ImageStatus resp = new ImageStatus();
        resp.setCompressed(true);
        resp.setCompressedBase64Data(message.getCompressedBase64Data());
        imageStatusService.saveImageStatus(resp);
    }
}
