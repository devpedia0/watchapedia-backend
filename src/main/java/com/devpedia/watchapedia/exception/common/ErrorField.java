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
public class ErrorField {
    private String field;
    private String input;
    private String message;

    private ErrorField(String field, String input, String message) {
        this.field = field;
        this.input = input;
        this.message = message;
    }

    public static ErrorField of(String field, String value, String message) {
        return new ErrorField(field, value, message);
    }

    public static List<ErrorField> listFrom(final BindingResult bindingResult) {
        final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors.stream()
                .map(error -> new ErrorField(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());
    }
}
