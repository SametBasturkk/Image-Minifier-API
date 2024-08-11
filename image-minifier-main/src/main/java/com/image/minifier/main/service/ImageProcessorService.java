package com.image.minifier.main.service;


import com.image.minifier.common.model.ImageStatus;
import com.image.minifier.common.service.ImageStatusService;
import com.image.minifier.common.util.FileUtil;
import com.image.minifier.main.dto.CompressedImageResponse;
import com.image.minifier.main.exception.FileProcessingException;
import com.image.minifier.main.exception.UnsupportedFileTypeException;
import com.kafka.producer.service.KafkaPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImageProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessorService.class);

    @Value("${app.supported.extensions}")
    private String[] supportedExtensions;

    private final FileUtil fileUtil;
    private final KafkaPublisherService kafkaPublisherService;
    private final StatisticsService statisticsService;
    private final ImageStatusService imageStatusService;

    public ImageProcessorService(FileUtil fileUtil, KafkaPublisherService kafkaPublisherService, StatisticsService statisticsService, ImageStatusService imageStatusService) {
        this.fileUtil = fileUtil;
        this.kafkaPublisherService = kafkaPublisherService;
        this.statisticsService = statisticsService;
        this.imageStatusService = imageStatusService;
    }

    public ResponseEntity<CompressedImageResponse> processImage(MultipartFile file, Integer quality) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !isSupportedExtension(extension)) {
            throw new UnsupportedFileTypeException("Unsupported file type: " + extension);
        }

        try {
            UUID compressedFilePathUUID = kafkaPublisherService.publishCompressImageTopic(file, quality, "." + extension.toLowerCase());

            ImageStatus imageStatus = new ImageStatus(compressedFilePathUUID, false, null);
            imageStatusService.saveImageStatus(imageStatus);
            while (imageStatusService.getImageStatusByUuid(imageStatus) == null) {
                Thread.sleep(100);
            }

            Path compressedFilePath = fileUtil.createCompressedDirectory().resolve(compressedFilePathUUID + "." + extension.toLowerCase());

            long originalSize = file.getSize();
            long compressedSize = Files.size(compressedFilePath);
            float compressionRatio = ((float) (originalSize - compressedSize) / originalSize) * 100;

            logger.info("Original size: {}, Compressed size: {}, Compression ratio: {}%", originalSize, compressedSize, compressionRatio);

            byte[] compressedImageData = Files.readAllBytes(compressedFilePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(file.getOriginalFilename()).build());
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());

            CompressedImageResponse response = new CompressedImageResponse(compressedImageData, file.getOriginalFilename(), originalSize, compressedSize, compressionRatio);


            statisticsService.updateCounterStatistic(compressedSize, originalSize);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (IOException e) {
            logger.error("Error processing image: {}", e.getMessage());
            throw new FileProcessingException("Error processing image", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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