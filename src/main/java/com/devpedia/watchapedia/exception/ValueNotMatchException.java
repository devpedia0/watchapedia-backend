package com.devpedia.watchapedia.exception;

import com.devpedia.watchapedia.exception.common.BusinessException;
import com.devpedia.watchapedia.exception.common.ErrorCode;

public class ValueNotMatchException extends BusinessException {
    public ValueNotMatchException(ErrorCode errorCode) {
        super(errorCode);
    }
}
