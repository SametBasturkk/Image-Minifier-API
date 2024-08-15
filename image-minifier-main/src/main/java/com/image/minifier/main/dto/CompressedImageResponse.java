package com.image.minifier.main.dto;

import lombok.Data;

@Data
public class CompressedImageResponse {
    private byte[] compressedImage;
    private String filename;
    private long originalSize;
    private long compressedSize;
    private double compressionRatio;

    public CompressedImageResponse(byte[] compressedImage, String filename, long originalSize, long compressedSize, double compressionRatio) {
        this.compressedImage = compressedImage;
        this.filename = filename;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionRatio = compressionRatio;
    }

}