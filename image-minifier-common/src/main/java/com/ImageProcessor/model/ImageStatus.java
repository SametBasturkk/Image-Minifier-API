package com.ImageProcessor.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

@Data
@NoArgsConstructor
@RedisHash("ImageStatus")
public class ImageStatus {
    @Id
    private Integer id;
    private UUID uuid;
    boolean isCompressed;

    public ImageStatus(UUID uuid, boolean isCompressed) {
        this.uuid = uuid;
        this.isCompressed = isCompressed;
    }
}
