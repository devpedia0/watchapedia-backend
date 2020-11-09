package com.devpedia.watchapedia.exception.common;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private ErrorCode errorCode;
    private List<FieldError> errors;

    public BusinessException(ErrorCode errorCode, String message, List<FieldError> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, new ArrayList<>());
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, List<FieldError> errors) {
        this(errorCode, errorCode.getMessage(), errors);
    }
}
