package com.devpedia.watchapedia.exception.handler;

import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.ExternalIOException;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.exception.common.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Log4j2
public class BusinessExceptionHandler {
    @ExceptionHandler(EntityNotExistException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(EntityNotExistException e) {
        ErrorResponse response = ErrorResponse.from(e);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(ValueDuplicatedException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(ValueDuplicatedException e) {
        ErrorResponse response = ErrorResponse.from(e);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(ValueNotMatchException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(ValueNotMatchException e) {
        ErrorResponse response = ErrorResponse.from(e);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler(ExternalIOException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(ExternalIOException e) {
        ErrorResponse response = ErrorResponse.from(e);
        return new ResponseEntity<>(response, response.getStatus());
    }
}
