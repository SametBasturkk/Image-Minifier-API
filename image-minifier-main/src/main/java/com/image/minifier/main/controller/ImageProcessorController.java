package com.image.minifier.main.controller;


import com.image.minifier.main.dto.CompressedImageResponse;
import com.image.minifier.main.service.ImageProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@Validated
public class ImageProcessorController {
    private final ImageProcessorService imageProcessorService;

    public ImageProcessorController(ImageProcessorService imageProcessorService) {
        this.imageProcessorService = imageProcessorService;
    }

    @Operation(summary = "Upload and compress an image")
    @ApiResponse(responseCode = "200", description = "Image compressed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "415", description = "Unsupported media type")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompressedImageResponse> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("quality") @Min(0) @Max(100) Integer quality) {
        return imageProcessorService.processImage(file, quality);
    }
}