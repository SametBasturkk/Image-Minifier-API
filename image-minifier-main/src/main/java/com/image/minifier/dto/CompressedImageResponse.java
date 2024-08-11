package com.image.minifier.dto;

import lombok.Data;

@Data
public class CompressedImageResponse {
    private byte[] compressedImage;
    private String filename;
    private long originalSize;
    private long compressedSize;
    private float compressionRatio;

    public CompressedImageResponse(byte[] compressedImage, String filename, long originalSize, long compressedSize, float compressionRatio) {
        this.compressedImage = compressedImage;
        this.filename = filename;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionRatio = compressionRatio;
    }

}