package com.image.minifier.common.service;

import com.image.minifier.common.model.ImageStatus;
import com.image.minifier.common.util.ModelMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ImageStatusService {

    private StringRedisTemplate redisTemplate;
    private ModelMapper modelMapper;

    public ImageStatusService(StringRedisTemplate redisTemplate, ModelMapper modelMapper) {
        this.redisTemplate = redisTemplate;
        this.modelMapper = modelMapper;
    }

    public void saveImageStatus(ImageStatus imageStatus) {
        redisTemplate.opsForValue().set(imageStatus.getUuid().toString(), imageStatus.toString());
    }

    public ImageStatus getImageStatusByUuid(ImageStatus imageStatus) {
        String imageStatusString = redisTemplate.opsForValue().get(imageStatus.getUuid().toString());
        return modelMapper.map(imageStatusString, ImageStatus.class);
    }

    public ImageStatus getImageStatusByUuid(UUID uuid) {
        String imageStatusString = redisTemplate.opsForValue().get(uuid.toString());
        return modelMapper.map(imageStatusString, ImageStatus.class);
    }
}
