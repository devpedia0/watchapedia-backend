package com.devpedia.watchapedia.dto.enums;

import org.springframework.core.convert.converter.Converter;

public class InterestContentOrderConverter implements Converter<String, InterestContentOrder> {
    @Override
    public InterestContentOrder convert(String source) {
        return InterestContentOrder.from(source);
    }
}
