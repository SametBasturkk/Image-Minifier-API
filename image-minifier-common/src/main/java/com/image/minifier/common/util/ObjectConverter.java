package com.image.minifier.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ObjectConverter {
    private ObjectMapper objectMapper;

    public ObjectConverter() {
        objectMapper = new ObjectMapper();
    }


    public String mapToString(Object source) {
        try {
            log.info("Converting object to string: {}", source.toString().length());
            return objectMapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T stringToMap(String source, Class<T> targetType) {
        try {
            log.info("Converting string to object: {}", source.length());
            return objectMapper.readValue(source, targetType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}