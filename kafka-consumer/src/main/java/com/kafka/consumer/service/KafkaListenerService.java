package com.kafka.consumer.service;


import com.ImageProcessor.model.CompressImageTopicModel;
import com.ImageProcessor.model.ImageStatus;
import com.ImageProcessor.repository.ImageStatusRepository;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

@EnableKafka
@KafkaListener(topics = "compress-image-topic", groupId = "group_id")
public class KafkaListenerService {

    private ImageStatusRepository imageStatusRepository;

    public KafkaListenerService(ImageStatusRepository imageStatusRepository) {
        this.imageStatusRepository = imageStatusRepository;
    }


    @KafkaListener(topics = "compress-image-topic-resp", groupId = "group_id")
    public void consume(CompressImageTopicModel message) {
        ImageStatus resp = imageStatusRepository.findByUuid(message.getUuid());
        resp.setCompressed(true);
        resp.setCompressedBase64Data(message.getCompressedBase64Data());
        imageStatusRepository.save(resp);
    }
}
