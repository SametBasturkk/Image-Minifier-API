package com.image.minifier.common.repository;

import com.image.minifier.common.model.ImageStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ImageStatusRepository extends CrudRepository<ImageStatus, Integer> {
    ImageStatus findByUuid(UUID uuid);
}
