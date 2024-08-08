package com.ImageProcessor.ImageProcessor.service;

import com.ImageProcessor.ImageProcessor.dto.CompressedImageResponse;
import com.ImageProcessor.ImageProcessor.exception.FileProcessingException;
import com.ImageProcessor.ImageProcessor.exception.UnsupportedFileTypeException;
import com.ImageProcessor.ImageProcessor.imageCompressor.ImageCompressor;
import com.ImageProcessor.ImageProcessor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessorService.class);

    @Value("${app.supported.extensions}")
    private String[] supportedExtensions;

    private final FileUtil fileUtil;
    private final ImageCompressor imageCompressor;

    public ImageProcessorService(FileUtil fileUtil, ImageCompressor imageCompressor) {
        this.fileUtil = fileUtil;
        this.imageCompressor = imageCompressor;
    }

    @Async
    public CompletableFuture<ResponseEntity<CompressedImageResponse>> uploadAndCompressImage(MultipartFile file, Integer quality) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !isSupportedExtension(extension)) {
            throw new UnsupportedFileTypeException("Unsupported file type: " + extension);
        }

        try {
            Path uploadedFilePath = fileUtil.saveUploadedFile(file);
            Path compressedFilePath = imageCompressor.compressImage(uploadedFilePath, quality, "." + extension.toLowerCase());

            long originalSize = file.getSize();
            long compressedSize = Files.size(compressedFilePath);
            float compressionRatio = ((float) (originalSize - compressedSize) / originalSize) * 100;

            logger.info("Original size: {}, Compressed size: {}, Compression ratio: {}%", originalSize, compressedSize, compressionRatio);

            byte[] compressedImageData = Files.readAllBytes(compressedFilePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(file.getOriginalFilename()).build());
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());

            CompressedImageResponse response = new CompressedImageResponse(compressedImageData, file.getOriginalFilename(), originalSize, compressedSize, compressionRatio);

            return CompletableFuture.completedFuture(ResponseEntity.ok().headers(headers).contentLength(compressedImageData.length).contentType(MediaType.APPLICATION_OCTET_STREAM).body(response));

        } catch (IOException e) {
            logger.error("Error processing image: {}", e.getMessage());
            throw new FileProcessingException("Error processing image", e);
        }
    }

    private boolean isSupportedExtension(String extension) {
        for (String supportedExtension : supportedExtensions) {
            if (supportedExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}