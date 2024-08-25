package com.image.minifier.main.controller;


import com.image.minifier.main.dto.CompressedImageResponse;
import com.image.minifier.main.service.ImageProcessorService;
import com.image.minifier.main.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/images")
@Validated
public class ImageProcessorController {
    private final ImageProcessorService imageProcessorService;
    private final UserService userService;

    public ImageProcessorController(ImageProcessorService imageProcessorService, UserService userService) {
        this.imageProcessorService = imageProcessorService;
        this.userService = userService;
    }

    @Operation(summary = "Upload and compress an image")
    @ApiResponse(responseCode = "200", description = "Image compressed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "415", description = "Unsupported media type")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompressedImageResponse> uploadImage(@RequestParam("file") MultipartFile file,
                                                               @RequestParam("quality") @Min(0) @Max(100) Integer quality,
                                                               @RequestParam("api_key") String apiKey,
                                                               @RequestHeader("Authorization") String token) {
        log.info("Received request to upload and compress image: {}", file.getOriginalFilename());
        return imageProcessorService.processImage(file, quality, apiKey, token);
    }


}