package com.ImageProcessor.repository;

import com.ImageProcessor.model.ImageStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageStatusRepository extends CrudRepository<ImageStatus, Integer> {
    ImageStatus findByUuid(UUID compressedFilePathUUID);
}
