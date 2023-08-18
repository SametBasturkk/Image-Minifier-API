package com.ImageProcessor.ImageProcessor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

  private final ImageProcessor imageProcessor;

  public UploadController(ImageProcessor imageProcessor) {
    this.imageProcessor = imageProcessor;
  }

  @PostMapping("/uploadImage")
  public ResponseEntity<ByteArrayResource> uploadAndCompressImage(@RequestParam("file") MultipartFile file,
      @RequestParam("quality") Integer quality, Model model) {
    return imageProcessor.uploadAndCompressImage(file, quality, model);
  }
}
