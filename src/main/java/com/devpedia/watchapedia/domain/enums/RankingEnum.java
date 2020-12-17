package com.devpedia.watchapedia.domain.enums;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {RankingEnumValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RankingEnum {
    String message() default "This is not Invalid value \n";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends java.lang.Enum<?>> enumClass();
    boolean ignoreCase() default false;
}
