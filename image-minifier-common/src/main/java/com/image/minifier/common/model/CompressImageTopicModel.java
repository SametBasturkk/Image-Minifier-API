package com.image.minifier.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CompressImageTopicModel {
    private byte[] inputFile;
    private Integer quality;
    private String extension;
    private UUID uuid;
    private byte[] compressedBase64Data;

    public CompressImageTopicModel(byte[] inputFile, Integer quality, String extension, UUID uuid, byte[] compressedBase64Data) {
        this.inputFile = inputFile;
        this.quality = quality;
        this.extension = extension;
        this.uuid = uuid;
        this.compressedBase64Data = compressedBase64Data;
    }
}
