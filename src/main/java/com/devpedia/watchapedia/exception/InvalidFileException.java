package com.devpedia.watchapedia.exception;

import com.devpedia.watchapedia.exception.common.BusinessException;
import com.devpedia.watchapedia.exception.common.ErrorCode;

public class InvalidFileException extends BusinessException {
    public InvalidFileException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
