package com.devpedia.watchapedia.exception;

import com.devpedia.watchapedia.exception.common.BusinessException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.common.ErrorField;

public class ValueDuplicatedException extends BusinessException {
    public ValueDuplicatedException(ErrorCode errorCode, ErrorField... errors) {
        super(errorCode, errors);
    }
}
