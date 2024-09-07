package com.image.minifier.main.service;

import com.image.minifier.common.util.ObjectConverter;
import com.image.minifier.main.model.ImageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ImageStatusService {

    private StringRedisTemplate redisTemplate;


    private ObjectConverter mapper;

    public ImageStatusService(StringRedisTemplate redisTemplate, ObjectConverter mapper) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }

    public void saveImageStatus(ImageStatus imageStatus) {
        log.info("Saving image status: {}", imageStatus);
        redisTemplate.opsForValue().set(imageStatus.getUuid().toString(), mapper.mapToString(imageStatus));
    }


    public ImageStatus getImageStatusByUuid(UUID uuid) {
        log.info("Getting image status by uuid: {}", uuid);
        String imageStatusString = redisTemplate.opsForValue().get(uuid.toString());
        return mapper.stringToMap(imageStatusString, ImageStatus.class);
    }

    public void deleteImageStatus(ImageStatus status) {
        log.info("Deleting image status: {}", status);
        redisTemplate.delete(status.getUuid().toString());
    }
}
