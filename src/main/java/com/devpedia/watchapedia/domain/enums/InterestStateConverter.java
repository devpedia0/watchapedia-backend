package com.devpedia.watchapedia.domain.enums;

import javax.persistence.AttributeConverter;

public class InterestStateConverter implements AttributeConverter<InterestState, Integer> {
    @Override
    public Integer convertToDatabaseColumn(InterestState attribute) {
        return attribute.getCode();
    }

    @Override
    public InterestState convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return InterestState.ofCode(dbData);
    }
}
