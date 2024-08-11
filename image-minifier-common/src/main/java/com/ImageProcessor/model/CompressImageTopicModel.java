package com.ImageProcessor.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CompressImageTopicModel {
    private Path inputFile;
    private Integer quality;
    private String extension;
    private UUID uuid;

    public CompressImageTopicModel(Path inputFile, Integer quality, String extension, UUID uuid) {
        this.inputFile = inputFile;
        this.quality = quality;
        this.extension = extension;
        this.uuid = uuid;
    }

}
