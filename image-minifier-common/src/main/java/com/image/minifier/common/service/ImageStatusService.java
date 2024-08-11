package com.image.minifier.common.service;

import com.image.minifier.common.model.ImageStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImageStatusService {

    private StringRedisTemplate redisTemplate;

    public ImageStatusService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveImageStatus(ImageStatus imageStatus) {
        redisTemplate.opsForValue().set(imageStatus.getUuid().toString(), imageStatus.toString());
    }

    public String getImageStatusByUuid(ImageStatus imageStatus) {
        String imageStatusString = redisTemplate.opsForValue().get(imageStatus.getUuid().toString());
        return imageStatusString;
    }
}
