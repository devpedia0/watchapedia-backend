package com.devpedia.watchapedia.dto.enums;

import org.springframework.core.convert.converter.Converter;

public class RatingContentOrderConverter implements Converter<String, RatingContentOrder> {
    @Override
    public RatingContentOrder convert(String source) {
        return RatingContentOrder.valueOf(source);
    }
}
