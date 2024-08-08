package com.ImageProcessor.ImageProcessor.imageCompressor;

import com.ImageProcessor.ImageProcessor.exception.CompressionException;
import com.ImageProcessor.ImageProcessor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class ImageCompressor {
    private static final Logger logger = LoggerFactory.getLogger(ImageCompressor.class);

    @Value("${app.compression.pngquant}")
    private String pngquantCommand;

    @Value("${app.compression.jpegoptim}")
    private String jpegoptimCommand;

    private final FileUtil fileUtil;

    public ImageCompressor(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public Path compressImage(Path inputFile, Integer quality, String extension) {
        try {
            logger.info("Compressing image: {}", inputFile);
            String command = extension.equals(FileUtil.PNG_EXTENSION)
                    ? String.format(pngquantCommand, fileUtil.createCompressedDirectory().resolve(inputFile.getFileName()), quality, inputFile)
                    : String.format(jpegoptimCommand, fileUtil.createCompressedDirectory(), quality, inputFile);
            executeCompressionCommand(command);
            return fileUtil.createCompressedDirectory().resolve(inputFile.getFileName());
        } catch (IOException | InterruptedException e) {
            throw new CompressionException("Error during image compression", e);
        }
    }

    private void executeCompressionCommand(String command) throws CompressionException, IOException, InterruptedException {
        logger.info("Compression command: {}", command);

        String[] shellCommand = System.getProperty("os.name").toLowerCase().contains("win")
                ? new String[]{"cmd", "/c", command}
                : new String[]{"/bin/sh", "-c", "./" + command};

        try {
            Process process = Runtime.getRuntime().exec(shellCommand);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new CompressionException("Compression command failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new CompressionException("Error executing compression command", e);
        }
    }
}