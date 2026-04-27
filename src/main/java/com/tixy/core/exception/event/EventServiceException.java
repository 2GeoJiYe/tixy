package com.tixy.core.exception.event;

import com.tixy.core.exception.BusinessException;
import com.tixy.core.exception.ErrorCode;

public class EventServiceException extends BusinessException {
    public EventServiceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
