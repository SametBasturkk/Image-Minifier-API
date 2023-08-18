
# Image Processor

The **Image Processor** is a Spring Boot application designed to upload, compress, and serve images. It supports both PNG and JPEG image formats and utilizes external tools like `pngquant` and `jpegoptim` for efficient compression.

## Table of Contents

-   [Description](https://chat.openai.com/c/5c159005-701d-4eb7-bdab-ab84a9d5d977#description)
-   [Endpoints](https://chat.openai.com/c/5c159005-701d-4eb7-bdab-ab84a9d5d977#endpoints)
-   [Classes](https://chat.openai.com/c/5c159005-701d-4eb7-bdab-ab84a9d5d977#classes)
-   [Usage](https://chat.openai.com/c/5c159005-701d-4eb7-bdab-ab84a9d5d977#usage)

## Description

The Image Processor allows users to upload images in either PNG or JPEG format. Upon upload, the application compresses the image based on the specified quality and serves the compressed image for download.

## Endpoints

### `/uploadImage`

This endpoint handles image upload and compression.

-   **Method:** POST
-   **Parameters:**
    -   `file`: The image file to upload (MultipartFile)
    -   `quality`: The quality parameter for JPEG compression (Integer)
-   **Response:** Compressed image as a downloadable file

## Classes

### `ImageProcessor`

This class is responsible for handling image upload, compression, and serving compressed images.

-   `uploadAndCompressImage`: Handles image upload, compression, and serves the compressed image.

### `UploadController`

This class defines a REST controller for handling image upload and compression requests.

-   `uploadAndCompressImage`: Forwards the upload request to the `ImageProcessor` and serves the compressed image.

### `MainApplication`

This class contains the main method to start the Spring Boot application.

## Usage

1.  Start the Spring Boot application.
2.  Access the `/uploadImage` endpoint to upload and compress images.
3.  Specify the `file` parameter (image) to upload and the `quality` parameter (for JPEG compression).
4.  Compressed images are available for download with the provided link.

Please note that the `pngquant` and `jpegoptim` tools must be available on the system where the application is deployed.

## Dependencies

-   Spring Boot
-   `pngquant` (for PNG image compression)
-   `jpegoptim` (for JPEG image compression)
