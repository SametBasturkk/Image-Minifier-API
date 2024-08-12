package com.image.minifier.common.util;

import org.springframework.stereotype.Component;

@Component
public class ModelMapper {


    public <T> T map(Object source, Class<T> destination) {
        try {
            T dest = destination.getDeclaredConstructor().newInstance();
            org.modelmapper.ModelMapper modelMapper = new org.modelmapper.ModelMapper();
            modelMapper.map(source, dest);
            return dest;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping objects", e);
        }
    }
}
