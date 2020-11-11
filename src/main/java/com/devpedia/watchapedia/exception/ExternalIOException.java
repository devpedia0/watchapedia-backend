package com.devpedia.watchapedia.exception;

import com.devpedia.watchapedia.exception.common.BusinessException;
import com.devpedia.watchapedia.exception.common.ErrorCode;

public class ExternalIOException extends BusinessException {
    public ExternalIOException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
