package com.devpedia.watchapedia.domain.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class AccessRangeConverter implements AttributeConverter<AccessRange, Integer> {
    @Override
    public Integer convertToDatabaseColumn(AccessRange attribute) {
        return attribute.getCode();
    }

    @Override
    public AccessRange convertToEntityAttribute(Integer dbData) {
        return AccessRange.ofCode(dbData);
    }
}
