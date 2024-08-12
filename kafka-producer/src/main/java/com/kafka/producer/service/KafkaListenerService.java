package com.kafka.producer.service;


import com.image.minifier.common.model.CompressImageTopicModel;
import com.image.minifier.common.model.ImageStatus;
import com.image.minifier.common.service.ImageStatusService;
import com.image.minifier.common.util.ModelMapper;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@EnableKafka
@Service
public class KafkaListenerService {

    private ImageStatusService imageStatusService;
    private ModelMapper modelMapper;

    public KafkaListenerService(ImageStatusService imageStatusService, ModelMapper modelMapper) {
        this.imageStatusService = imageStatusService;
        this.modelMapper = modelMapper;
    }


    @KafkaListener(topics = "compress-image-topic-resp", groupId = "group_id")
    public void consume(String message) {
        CompressImageTopicModel data = modelMapper.map(message, CompressImageTopicModel.class);
        ImageStatus imageStatus = imageStatusService.getImageStatusByUuid(data.getUuid());
        imageStatus.setCompressedBase64Data(data.getCompressedBase64Data());
        imageStatus.setCompressed(true);
        imageStatusService.saveImageStatus(imageStatus);
    }
}
