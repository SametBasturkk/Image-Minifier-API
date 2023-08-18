package com.ImageProcessor.ImageProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(maxAge = 3600)
@Service
public class ImageProcessor {

  private static final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
  private static final String UPLOAD_DIR = "./uploads/";
  private static final String COMPRESSED_DIR = "./compressed/";
  private static final String PNG_EXTENSION = ".png";
  private static final String JPG_EXTENSION = ".jpg";
  private static final String JPEG_EXTENSION = ".jpeg";
  private static final String PNGQUANT_COMMAND_TEMPLATE = "./pngquant --output \"%s\" --quality %d --speed 1 --force --strip \"%s\"";
  private static final String JPEGOPTIM_COMMAND_TEMPLATE = "./jpegoptim --dest \"%s\" -o -m %d -s \"%s\"";

  public ResponseEntity<ByteArrayResource> uploadAndCompressImage(MultipartFile file,
      Integer quality, Model model) {
    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
    String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."))
        .toLowerCase();

    switch (fileExtension) {
      case PNG_EXTENSION:
        return processAndRespond(file, quality, model, PNG_EXTENSION);
      case JPG_EXTENSION:
      case JPEG_EXTENSION:
        return processAndRespond(file, quality, model, JPG_EXTENSION);
      default:
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
    }
  }

  private ResponseEntity<ByteArrayResource> processAndRespond(MultipartFile file, Integer quality,
      Model model, String extension) {
    try {
      Path uploadDirectory = Paths.get(UPLOAD_DIR);
      Files.createDirectories(uploadDirectory);

      String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
      Path uploadedFilePath = uploadDirectory.resolve(originalFileName);
      Files.copy(file.getInputStream(), uploadedFilePath, StandardCopyOption.REPLACE_EXISTING);

      long originalSize = file.getSize();
      Path compressedFilePath = compressImage(uploadedFilePath, quality, extension);
      long compressedSize = Files.size(compressedFilePath);
      float compressionRatio = ((float) (originalSize - compressedSize) / originalSize) * 100;

      logCompressionInfo(originalSize, compressedSize, compressionRatio);

      model.addAttribute("message", "File uploaded and compressed successfully");

      byte[] compressedImageData = Files.readAllBytes(compressedFilePath);
      ByteArrayResource resource = new ByteArrayResource(compressedImageData);

      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"" + originalFileName.replace("\"", "\\\"") + "\"");
      headers.setCacheControl("no-cache, no-store, must-revalidate");
      headers.setPragma("no-cache");
      headers.setExpires(0);

      ResponseEntity<ByteArrayResource> responseEntity = ResponseEntity
          .status(HttpStatus.OK) // Add the HTTP OK status here
          .headers(headers)
          .contentLength(compressedImageData.length)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(resource);

      //cleanupFiles(uploadedFilePath, compressedFilePath);

      return responseEntity;
    } catch (IOException e) {
      handleProcessingError(model, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private Path compressImage(Path inputFile, Integer quality, String extension) throws IOException {
    Path compressedDirectory = Paths.get(COMPRESSED_DIR);
    Files.createDirectories(compressedDirectory);

    String compressedFileName = inputFile.getFileName().toString();
    Path compressedFilePath = compressedDirectory.resolve(compressedFileName);

    String command = createCompressionCommand(extension, quality, inputFile, compressedFilePath);
    executeCompressionCommand(command);

    return compressedFilePath;
  }

  private String createCompressionCommand(String extension, Integer quality, Path inputFile,
      Path compressedFilePath) {
    if (extension.equals(PNG_EXTENSION)) {
      return String.format(PNGQUANT_COMMAND_TEMPLATE, compressedFilePath, quality, inputFile);
    } else if (extension.equals(JPG_EXTENSION) || extension.equals(JPEG_EXTENSION)) {
      return String.format(JPEGOPTIM_COMMAND_TEMPLATE, COMPRESSED_DIR, quality, inputFile);
    }
    return "";
  }

  private void executeCompressionCommand(String command) throws IOException {
    logger.info("Compression command: {}", command);

    try {
      String[] shellCommand = {"/bin/sh", "-c", command};

      Process process = Runtime.getRuntime().exec(shellCommand);
      process.waitFor();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Compression process interrupted: {}", e.getMessage());
    }
  }

  private void logCompressionInfo(long originalSize, long compressedSize, float compressionRatio) {
    logger.info("Original file size: {}", originalSize);
    logger.info("Compressed file size: {}", compressedSize);
    logger.info("Compression ratio: {}%", compressionRatio);
  }

  private void cleanupFiles(Path uploadedFilePath, Path compressedFilePath) throws IOException {
    Files.delete(uploadedFilePath);
    Files.delete(compressedFilePath);
  }

  private void handleProcessingError(Model model, IOException e) {
    e.printStackTrace();
    model.addAttribute("message", "Error uploading and compressing file: " + e.getMessage());
    logger.error("Error uploading and compressing file: {}", e.getMessage());
  }
}
