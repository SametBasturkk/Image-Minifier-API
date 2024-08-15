package com.image.minifier.main.service;

import com.image.minifier.common.util.FileUtil;
import com.image.minifier.main.dto.CompressedImageResponse;
import com.image.minifier.main.exception.FileProcessingException;
import com.image.minifier.main.exception.UnsupportedFileTypeException;
import com.image.minifier.main.model.ImageStatus;
import com.image.minifier.main.producer.KafkaPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class ImageProcessorService {

    @Value("${app.supported.extensions}")
    private String[] supportedExtensions;

    private final FileUtil fileUtil;
    private final KafkaPublisherService kafkaPublisherService;
    private final StatisticsService statisticsService;
    private final ImageStatusService imageStatusService;
    private final ScheduledExecutorService executorService;

    public ImageProcessorService(FileUtil fileUtil, KafkaPublisherService kafkaPublisherService, StatisticsService statisticsService, ImageStatusService imageStatusService, ScheduledExecutorService executorService) {
        this.fileUtil = fileUtil;
        this.kafkaPublisherService = kafkaPublisherService;
        this.statisticsService = statisticsService;
        this.imageStatusService = imageStatusService;
        this.executorService = executorService;
    }

    public ResponseEntity<CompressedImageResponse> processImage(MultipartFile file, Integer quality) {
        validateFile(file);
        String extension = getExtension(file);

        try {
            UUID compressedFilePathUUID = kafkaPublisherService.publishCompressImageTopic(file, quality, "." + extension.toLowerCase());
            ImageStatus imageStatus = new ImageStatus(compressedFilePathUUID, false, null);
            imageStatusService.saveImageStatus(imageStatus);
            log.info("Image processing started for file: {}", file.getOriginalFilename());

            return waitForImageCompression(imageStatus, file);
        } catch (IOException e) {
            log.error("Error processing image", e);
            throw new FileProcessingException("Error processing image", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !isSupportedExtension(extension)) {
            throw new UnsupportedFileTypeException("Unsupported or unrecognized file type");
        }
    }

    private String getExtension(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null) {
            throw new UnsupportedFileTypeException("File type could not be determined");
        }
        return extension;
    }

    private boolean isSupportedExtension(String extension) {
        return Arrays.stream(supportedExtensions).anyMatch(extension::equalsIgnoreCase);
    }

    private ResponseEntity<CompressedImageResponse> waitForImageCompression(ImageStatus imageStatus, MultipartFile file) {
        CompletableFuture<ResponseEntity<CompressedImageResponse>> responseFuture = new CompletableFuture<>();

        log.info("Waiting for image compression to complete");


        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            ImageStatus status = imageStatusService.getImageStatusByUuid(imageStatus.getUuid());
            if (status.isCompressed()) {
                completeResponseFuture(responseFuture, status, file);
                log.info("Image compression completed for file: {}", file.getOriginalFilename());
                Thread.currentThread().interrupt();
            }
        }, 0, 1, TimeUnit.SECONDS);

        try {
            return responseFuture.get(30, TimeUnit.SECONDS); // Add a timeout
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Image compression process failed or timed out", e);
        } finally {
            scheduledFuture.cancel(true);
        }
    }

    private void completeResponseFuture(CompletableFuture<ResponseEntity<CompressedImageResponse>> responseFuture, ImageStatus status, MultipartFile file) {
        byte[] compressedImageData = status.getCompressedBase64Data();
        long originalSize = file.getSize();
        long compressedSize = compressedImageData.length;
        double compressionRatio = calculateCompressionRatio(originalSize, compressedSize);

        log.info("Original size: {}, Compressed size: {}, Compression ratio: {}%", originalSize, compressedSize, compressionRatio);

        CompressedImageResponse response = new CompressedImageResponse(compressedImageData, file.getOriginalFilename(), originalSize, compressedSize, compressionRatio);
        statisticsService.updateCounterStatistic(compressedSize, originalSize);

        responseFuture.complete(ResponseEntity.ok().headers(createHeaders(file)).contentType(MediaType.APPLICATION_JSON).body(response));
    }

    private double calculateCompressionRatio(long originalSize, long compressedSize) {
        return ((double) (originalSize - compressedSize) / originalSize) * 100 ;
    }

    private HttpHeaders createHeaders(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(file.getOriginalFilename()).build());
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        return headers;
    }
}