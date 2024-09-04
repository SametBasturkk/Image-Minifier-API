package com.image.minifier.main.service;

import com.image.minifier.main.dto.CompressedImageResponse;
import com.image.minifier.main.exception.FileProcessingException;
import com.image.minifier.main.model.ImageStatus;
import com.image.minifier.main.producer.KafkaPublisherService;
import com.image.minifier.main.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.VerificationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class ImageProcessorService {

    private final KafkaPublisherService kafkaPublisherService;
    private final StatisticsService statisticsService;
    private final ImageStatusService imageStatusService;
    private final ScheduledExecutorService executorService;
    private final UserService userService;
    private final UserRepository userRepository;

    public ImageProcessorService(KafkaPublisherService kafkaPublisherService, StatisticsService statisticsService, ImageStatusService imageStatusService, ScheduledExecutorService executorService, UserService userService, UserRepository userRepository) {
        this.kafkaPublisherService = kafkaPublisherService;
        this.statisticsService = statisticsService;
        this.imageStatusService = imageStatusService;
        this.executorService = executorService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public ResponseEntity<CompressedImageResponse> processImage(MultipartFile file, Integer quality, String apiKey, String token) throws VerificationException {
        String username = userService.validateApiKey(apiKey, token);
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename().toLowerCase());
        try {
            UUID compressedFilePathUUID = kafkaPublisherService.publishCompressImageTopic(file, quality, "." + extension);
            ImageStatus imageStatus = new ImageStatus(compressedFilePathUUID, false, null);
            imageStatusService.saveImageStatus(imageStatus);
            log.info("Image processing started for file: {}", file.getOriginalFilename());
            return waitForImageCompression(imageStatus, file, username);
        } catch (IOException e) {
            log.error("Error processing image", e);
            throw new FileProcessingException("Error processing image", e);
        }
    }


    private ResponseEntity<CompressedImageResponse> waitForImageCompression(ImageStatus imageStatus, MultipartFile file, String username) {
        CompletableFuture<ResponseEntity<CompressedImageResponse>> responseFuture = new CompletableFuture<>();

        log.info("Waiting for image compression to complete");


        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            ImageStatus status = imageStatusService.getImageStatusByUuid(imageStatus.getUuid());
            if (status.isCompressed()) {
                completeResponseFuture(responseFuture, status, file, username);
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

    private void completeResponseFuture(CompletableFuture<ResponseEntity<CompressedImageResponse>> responseFuture, ImageStatus status, MultipartFile file, String username) {
        byte[] compressedImageData = status.getCompressedBase64Data();
        long originalSize = file.getSize();
        long compressedSize = compressedImageData.length;
        double compressionRatio = ((double) (originalSize - compressedSize) / originalSize) * 100;

        log.info("Original size: {}, Compressed size: {}, Compression ratio: {}%", originalSize, compressedSize, compressionRatio);

        CompressedImageResponse response = new CompressedImageResponse(compressedImageData, file.getOriginalFilename(), originalSize, compressedSize, compressionRatio);
        statisticsService.updateCounterStatistic(compressedSize, originalSize, username);

        responseFuture.complete(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response));
    }


}