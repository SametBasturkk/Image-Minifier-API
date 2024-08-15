package com.image.minifier.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Slf4j
public class FileUtil {
    private String uploadDir = "./uploads/";

    private String COMPRESSED_DIR = "./compressed/";

    public static final String PNG_EXTENSION = ".png";
    public static final String JPG_EXTENSION = ".jpg";
    public static final String JPEG_EXTENSION = ".jpeg";

    public Path saveUploadedFile(byte[] file, UUID uuid) throws IOException {
        Path uploadDirectory = Files.createDirectories(Paths.get(uploadDir));
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