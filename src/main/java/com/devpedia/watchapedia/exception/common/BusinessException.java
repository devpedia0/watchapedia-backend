package com.devpedia.watchapedia.exception.common;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private ErrorCode errorCode;
    private List<ErrorField> errors;

    public BusinessException(ErrorCode errorCode, String message, List<ErrorField> errors) {
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

    public BusinessException(ErrorCode errorCode, List<ErrorField> errors) {
        this(errorCode, errorCode.getMessage(), errors);
    }

    public BusinessException(ErrorCode errorCode, ErrorField... errors) {
        this(errorCode, Arrays.asList(errors));
    }
}
