package com.kafka.consumer.compression;

import com.image.minifier.common.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
public class ImageCompressorService {

    @Value("${app.compression.pngquant}")
    private String pngquantCommand;

    @Value("${app.compression.jpegoptim}")
    private String jpegoptimCommand;

    private final FileUtil fileUtil;

    public ImageCompressorService(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public void compressImage(byte[] file, Integer quality, String extension, UUID uuid) {
        try {
            Path inputFile = fileUtil.saveUploadedFile(file, uuid);
            log.info("Compressing image: {}", inputFile);
            String command = extension.equals(FileUtil.PNG_EXTENSION)
                    ? String.format(pngquantCommand, fileUtil.createCompressedDirectory().resolve(uuid.toString()), quality, inputFile)
                    : String.format(jpegoptimCommand, fileUtil.createCompressedDirectory(), quality, inputFile);
            executeCompressionCommand(command);
        } catch (IOException | InterruptedException e) {
            log.error("Error compressing image", e);
        }
    }

    private void executeCompressionCommand(String command) throws IOException, InterruptedException {
        log.info("Compression command: {}", command);

        String[] shellCommand = System.getProperty("os.name").toLowerCase().contains("win")
                ? new String[]{"cmd", "/c", command}
                : new String[]{"/bin/sh", "-c", "./" + command};

        try {
            Process process = Runtime.getRuntime().exec(shellCommand);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Error executing compression command: {}", command);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error executing compression command", e);
        }
    }
}