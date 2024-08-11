package com.image.minifier.common.model;

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
    byte[] compressedBase64Data;

    public ImageStatus(UUID uuid, boolean isCompressed, byte[] compressedBase64Data) {
        this.uuid = uuid;
        this.isCompressed = isCompressed;
        this.compressedBase64Data = compressedBase64Data;
    }
}
