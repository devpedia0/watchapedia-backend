package com.devpedia.watchapedia.exception.handler;

import com.devpedia.watchapedia.exception.*;
import com.devpedia.watchapedia.exception.common.BusinessException;
import com.devpedia.watchapedia.exception.common.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Log4j2
public class BusinessExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BusinessException e) {
        ErrorResponse response = ErrorResponse.from(e);
        return new ResponseEntity<>(response, response.getStatus());
    }
}
