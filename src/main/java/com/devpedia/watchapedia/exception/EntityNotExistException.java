package com.devpedia.watchapedia.exception;

import com.devpedia.watchapedia.exception.common.BusinessException;
import com.devpedia.watchapedia.exception.common.ErrorCode;

public class EntityNotExistException extends BusinessException {
    public EntityNotExistException(ErrorCode errorCode) {
        super(errorCode);
    }
}
