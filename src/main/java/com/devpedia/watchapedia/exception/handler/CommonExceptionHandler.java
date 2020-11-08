package com.devpedia.watchapedia.exception.handler;

import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.common.ErrorResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Log4j2
public class CommonExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(MethodArgumentNotValidException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID, e.getBindingResult());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(ConstraintViolationException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(JwtException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(JwtException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.TOKEN_INVALID);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(MissingRequestHeaderException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }
}
