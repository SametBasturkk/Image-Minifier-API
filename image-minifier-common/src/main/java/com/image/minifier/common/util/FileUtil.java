package com.image.minifier.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Slf4j
public class FileUtil {

    @Value("${app.upload.dir}")
    public static String UPLOAD_DIR;

    @Value("${app.compression.dir}")
    public static String COMPRESSED_DIR;

    @Value("${app.compression.png.extension}")
    public static String PNG_EXTENSION;
    @Value("${app.compression.jpg.extension}")
    public static String JPG_EXTENSION;
    @Value("${app.compression.jpeg.extension}")
    public static String JPEG_EXTENSION;

    public Path saveUploadedFile(byte[] file, UUID uuid) throws IOException {
        Path uploadDirectory = Files.createDirectories(Paths.get(UPLOAD_DIR));
        log.info("Upload directory: {}", uploadDirectory);
        Path inputFile = uploadDirectory.resolve(uuid.toString());
        Files.write(inputFile, file);
        log.info("Saved file: {}", inputFile);
        return inputFile;
    }

    public Path createCompressedDirectory() throws IOException {
        log.info("Creating compressed directory: {}", COMPRESSED_DIR);
        return Files.createDirectories(Paths.get(COMPRESSED_DIR));
    }
}