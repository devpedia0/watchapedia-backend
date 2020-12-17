package com.devpedia.watchapedia.dto.enums;

import org.springframework.core.convert.converter.Converter;

public class ContentTypeParameterConverter implements Converter<String, ContentTypeParameter> {
    @Override
    public ContentTypeParameter convert(String source) {
        return ContentTypeParameter.from(source);
    }
}
