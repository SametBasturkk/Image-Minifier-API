package com.ImageProcessor.service;

import com.ImageProcessor.model.ImageStatus;
import com.ImageProcessor.repository.ImageStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class ImageStatusService {

    private ImageStatusRepository imageStatusRepository;

    public ImageStatusService(ImageStatusRepository imageStatusRepository) {
        this.imageStatusRepository = imageStatusRepository;
    }

    public void saveImageStatus(ImageStatus imageStatus) {
        ImageStatus resp = imageStatusRepository.findByUuid(imageStatus.getUuid());
        resp.setCompressed(true);
        resp.setCompressedBase64Data(imageStatus.getCompressedBase64Data());
        imageStatusRepository.save(resp);
    }

    public ImageStatus getImageStatusByUuid(ImageStatus imageStatus) {
        return imageStatusRepository.findByUuid(imageStatus.getUuid());
    }
}
