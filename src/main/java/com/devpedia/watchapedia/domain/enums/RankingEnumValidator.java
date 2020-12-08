package com.devpedia.watchapedia.domain.enums;

import com.devpedia.watchapedia.domain.Ranking;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RankingEnumValidator implements ConstraintValidator<RankingEnum, String> {

    private RankingEnum annotation;

    @Override
    public void initialize(RankingEnum constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = false;
        Object[] enumValues = this.annotation.enumClass().getEnumConstants();
        if (enumValues != null) {
            for (Object enumValue : enumValues) {
                if (value.equals(enumValue.toString())
                        || (this.annotation.ignoreCase() && value.equalsIgnoreCase(enumValue.toString()))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
