package com.kafka.producer.service;


import com.ImageProcessor.model.CompressImageTopicModel;
import com.ImageProcessor.model.ImageStatus;
import com.ImageProcessor.repository.ImageStatusRepository;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@EnableKafka
@Service
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
