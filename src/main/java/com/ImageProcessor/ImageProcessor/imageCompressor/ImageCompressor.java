package com.ImageProcessor.ImageProcessor.imageCompressor;

import com.ImageProcessor.ImageProcessor.exception.CompressionException;
import com.ImageProcessor.ImageProcessor.util.FileUtil;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.*;

@Component
public class ImageCompressor {
    private static final Logger logger = LoggerFactory.getLogger(ImageCompressor.class);

    @Value("${app.compression.pngquant}")
    private String pngquantCommand;

    @Value("${app.compression.jpegoptim}")
    private String jpegoptimCommand;

    private final ExecutorService executorService;

    public ImageCompressor() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public Path compressImage(Path inputFile, Integer quality, String extension) throws IOException {
        Path compressedDirectory = FileUtil.createCompressedDirectory();
        Path compressedFilePath = compressedDirectory.resolve(inputFile.getFileName());

        String command = extension.equals(FileUtil.PNG_EXTENSION) ? String.format(pngquantCommand, compressedFilePath, quality, inputFile) : String.format(jpegoptimCommand, FileUtil.COMPRESSED_DIR, quality, inputFile);

        try {
            CompletableFuture<Void> compressionFuture = CompletableFuture.runAsync(() -> {
                try {
                    executeCompressionCommand(command);
                } catch (IOException e) {
                    throw new CompressionException("Error during image compression", e);
                }
            }, executorService);

            compressionFuture.get(5, TimeUnit.MINUTES); // Set a timeout for compression
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new CompressionException("Error during image compression", e);
        }

        return compressedFilePath;
    }

    private void executeCompressionCommand(String command) throws IOException {
        logger.info("Compression command: {}", command);
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new CompressionException("Compression command failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompressionException("Compression process interrupted", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}