package com.devpedia.watchapedia.exception.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FieldError {
    private String field;
    private String input;
    private String message;

    private FieldError(String field, String input, String message) {
        this.field = field;
        this.input = input;
        this.message = message;
    }

    public static FieldError of(String field, String value, String reason) {
        return new FieldError(field, value, reason);
    }

    public static List<FieldError> listOf(String field, String value, String reason) {
        return Collections.singletonList(new FieldError(field, value, reason));
    }

    public static List<FieldError> listFrom(final BindingResult bindingResult) {
        final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors.stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());
    }
}
