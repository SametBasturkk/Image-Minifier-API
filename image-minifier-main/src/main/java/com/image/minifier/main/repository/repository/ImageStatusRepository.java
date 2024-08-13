package com.image.minifier.main.repository.repository;

import com.image.minifier.main.model.ImageStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ImageStatusRepository extends CrudRepository<ImageStatus, Integer> {
    ImageStatus findByUuid(UUID uuid);
}
