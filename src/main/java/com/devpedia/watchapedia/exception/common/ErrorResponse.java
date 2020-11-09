package com.devpedia.watchapedia.exception.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({"status", "code", "message", "errors"})
public class ErrorResponse {

    private HttpStatus status;
    private String code;
    private String message;
    private List<FieldError> errors;

    private ErrorResponse(ErrorCode code, String message, List<FieldError> errors) {
        this.status = code.getStatus();
        this.code = code.getCode();
        this.message = message;
        this.errors = errors;
    }

    @JsonGetter("status")
    public int status() {
        return status.value();
    }

    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code, message, new ArrayList<>());
    }

    public static ErrorResponse of(ErrorCode code) {
        return of(code, code.getMessage());
    }

    public static ErrorResponse of(ErrorCode code, BindingResult bindingResult) {
        return new ErrorResponse(code, code.getMessage(), FieldError.listFrom(bindingResult));
    }

    public static ErrorResponse from(BusinessException e) {
        return new ErrorResponse(e.getErrorCode(), e.getMessage(), e.getErrors());
    }

}