package com.image.minifier.main.producer;


import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.util.ObjectConverter;
import com.image.minifier.main.model.ImageStatus;
import com.image.minifier.main.service.ImageStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@EnableKafka
@Service
public class KafkaListenerService {

    private ImageStatusService imageStatusService;

    @Autowired
    private ObjectConverter mapper;


    public KafkaListenerService(ImageStatusService imageStatusService) {
        this.imageStatusService = imageStatusService;
    }

    @KafkaListener(topics = "compress-image-topic-resp", groupId = "group_id")
    public void consume(CompressImageTopicModel message) {
        ImageStatus imageStatus = imageStatusService.getImageStatusByUuid(message.getUuid());
        imageStatus.setCompressedBase64Data(message.getCompressedBase64Data());
        imageStatus.setCompressed(true);
        log.info("Consumed message: {}", message);
        imageStatusService.saveImageStatus(imageStatus);
    }
}
