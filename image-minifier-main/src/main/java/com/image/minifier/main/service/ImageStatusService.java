package com.image.minifier.main.service;

import com.image.minifier.common.util.ModelConverter;
import com.image.minifier.main.model.ImageStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ImageStatusService {

    private StringRedisTemplate redisTemplate;


    private ModelConverter mapper;

    public ImageStatusService(StringRedisTemplate redisTemplate , ModelConverter mapper) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }

    public void saveImageStatus(ImageStatus imageStatus) {
        redisTemplate.opsForValue().set(imageStatus.getUuid().toString(), mapper.mapToString(imageStatus));
    }

    public ImageStatus getImageStatusByUuid(ImageStatus imageStatus) {
        String imageStatusString = redisTemplate.opsForValue().get(imageStatus.getUuid().toString());
        return mapper.map(imageStatusString, ImageStatus.class);
    }

    public ImageStatus getImageStatusByUuid(UUID uuid) {
        String imageStatusString = redisTemplate.opsForValue().get(uuid.toString());
        return mapper.stringToMap(imageStatusString, ImageStatus.class);
    }
}
