package com.kafka.producer.service;


import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.model.ImageStatus;
import com.image.minifier.common.service.ImageStatusService;
import com.image.minifier.common.util.ModelConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@EnableKafka
@Service
public class KafkaListenerService {

    private ImageStatusService imageStatusService;

    @Autowired
    private ModelConverter mapper;


    public KafkaListenerService(ImageStatusService imageStatusService) {
        this.imageStatusService = imageStatusService;
    }

    @KafkaListener(topics = "compress-image-topic-resp", groupId = "group_id")
    public void consume(String message) {
        CompressImageTopicModel data = mapper.stringToMap(message, CompressImageTopicModel.class);
        ImageStatus imageStatus = imageStatusService.getImageStatusByUuid(data.getUuid());
        imageStatus.setCompressedBase64Data(data.getCompressedBase64Data());
        imageStatus.setCompressed(true);
        imageStatusService.saveImageStatus(imageStatus);
    }
}
