package com.kafka.producer.compression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ImageProcessor.util.FileUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImageCompressorService {
    public static final Logger logger = LoggerFactory.getLogger(ImageCompressorService.class);

    @Value("${app.compression.pngquant}")
    private String pngquantCommand;

    @Value("${app.compression.jpegoptim}")
    private String jpegoptimCommand;

    private final FileUtil fileUtil;

    public ImageCompressorService(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public void compressImage(MultipartFile file, Integer quality, String extension, UUID uuid) {
        try {
            Path inputFile = fileUtil.saveUploadedFile(file, uuid);
            logger.info("Compressing image: {}", inputFile);
            String command = extension.equals(FileUtil.PNG_EXTENSION)
                    ? String.format(pngquantCommand, fileUtil.createCompressedDirectory().resolve(uuid.toString()), inputFile)
                    : String.format(jpegoptimCommand, fileUtil.createCompressedDirectory(), quality, inputFile);
            executeCompressionCommand(command);
        } catch (IOException | InterruptedException e) {
            logger.error("Error compressing image", e);
        }
    }

    private void executeCompressionCommand(String command) throws IOException, InterruptedException {
        logger.info("Compression command: {}", command);

        String[] shellCommand = System.getProperty("os.name").toLowerCase().contains("win")
                ? new String[]{"cmd", "/c", command}
                : new String[]{"/bin/sh", "-c", "./" + command};

        try {
            Process process = Runtime.getRuntime().exec(shellCommand);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
               logger.error("Error executing compression command: {}", command);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing compression command", e);
        }
    }
}