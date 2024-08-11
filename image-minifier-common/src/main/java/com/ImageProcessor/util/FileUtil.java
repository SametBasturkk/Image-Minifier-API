package com.ImageProcessor.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileUtil {
    private String uploadDir = "./uploads/";

    private String COMPRESSED_DIR = "./compressed/";

    public static final String PNG_EXTENSION = ".png";
    public static final String JPG_EXTENSION = ".jpg";
    public static final String JPEG_EXTENSION = ".jpeg";

    public Path saveUploadedFile(MultipartFile file , UUID uuid) throws IOException {
        Path uploadDirectory = Files.createDirectories(Paths.get(uploadDir));
        Path filePath = uploadDirectory.resolve(uuid.toString());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    public Path createCompressedDirectory() throws IOException {
        return Files.createDirectories(Paths.get(COMPRESSED_DIR));
    }
}