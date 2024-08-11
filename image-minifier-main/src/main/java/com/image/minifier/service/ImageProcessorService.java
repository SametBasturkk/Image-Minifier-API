package com.image.minifier.service;


import com.ImageProcessor.model.ImageStatus;
import com.ImageProcessor.util.FileUtil;
import com.image.minifier.dto.CompressedImageResponse;
import com.image.minifier.exception.FileProcessingException;
import com.image.minifier.exception.UnsupportedFileTypeException;
import com.image.minifier.repository.ImageStatusRepository;
import com.kafka.consumer.service.KafkaPublisherService;
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
    private final ImageStatusRepository imageStatusRepository;

    public ImageProcessorService(FileUtil fileUtil, KafkaPublisherService kafkaPublisherService, StatisticsService statisticsService, ImageStatusRepository imageStatusRepository) {
        this.fileUtil = fileUtil;
        this.kafkaPublisherService = kafkaPublisherService;
        this.statisticsService = statisticsService;
        this.imageStatusRepository = imageStatusRepository;
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
            Path uploadedFilePath = fileUtil.saveUploadedFile(file);
            logger.info("Uploaded file: {}", uploadedFilePath);
            UUID compressedFilePathUUID = kafkaPublisherService.publishCompressImageTopic(uploadedFilePath, quality, "." + extension.toLowerCase());
            ImageStatus imageStatus = new ImageStatus(compressedFilePathUUID, false);
            imageStatusRepository.save(imageStatus);

            while (imageStatusRepository.findByUuid(compressedFilePathUUID).isCompressed()) {
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

            cleanFiles(uploadedFilePath, compressedFilePath);

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

    private void cleanFiles(Path uploadedFilePath, Path compressedFilePath) {
        try {
            Files.deleteIfExists(uploadedFilePath);
            Files.deleteIfExists(compressedFilePath);
            logger.info("Deleted files: {}, {}", uploadedFilePath, compressedFilePath);
        } catch (IOException e) {
            logger.error("Error deleting files: {}", e.getMessage());
        }
    }
}