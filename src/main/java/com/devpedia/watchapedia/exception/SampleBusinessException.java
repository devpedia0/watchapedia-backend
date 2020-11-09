package com.devpedia.watchapedia.exception;

import com.devpedia.watchapedia.exception.common.BusinessException;
import com.devpedia.watchapedia.exception.common.ErrorCode;

public class SampleBusinessException extends BusinessException {
    public SampleBusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
