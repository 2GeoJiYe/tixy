package com.tixy.core.exception.venue;

import com.tixy.core.exception.BusinessException;
import com.tixy.core.exception.ErrorCode;

public class VenueException extends BusinessException {
    public VenueException(ErrorCode errorCode) {
        super(errorCode);
    }
}
