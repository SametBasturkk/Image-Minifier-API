package com.image.minifier.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ModelConverter {
    private ModelMapper modelMapper;
    private ObjectMapper objectMapper;

    public ModelConverter() {
        modelMapper = new ModelMapper();
        objectMapper = new ObjectMapper();
    }


    public <T> T map(String source, Class<T> targetType) {
        return modelMapper.map(source, targetType);
    }

    public String mapToString(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T stringToMap(String source, Class<T> targetType) {
        try {
            return objectMapper.readValue(source, targetType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}