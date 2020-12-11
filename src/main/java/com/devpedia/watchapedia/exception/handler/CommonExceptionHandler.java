package com.devpedia.watchapedia.exception.handler;

import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.common.ErrorField;
import com.devpedia.watchapedia.exception.common.ErrorResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.*;

@ControllerAdvice
@Log4j2
public class CommonExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(RuntimeException e) {
        e.printStackTrace();
        String stackTrace = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(" "));
        log.error("{} {} {}",
                keyValue("exception_stack_trace", stackTrace),
                keyValue("exception_class", e.getClass().getSimpleName()),
                keyValue("exception_message", e.getMessage()));
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_ERROR);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(MethodArgumentNotValidException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID, e.getBindingResult());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(MethodArgumentTypeMismatchException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID,
                ErrorField.of(e.getParameter().getParameterName(), Objects.requireNonNull(e.getValue()).toString(), ""));
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(MissingServletRequestParameterException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(MissingServletRequestPartException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(HttpMessageNotReadableException e) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_VALUE_INVALID);
        return new ResponseEntity<>(response, response.getStatus());
    }
}
