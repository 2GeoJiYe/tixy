package com.tixy.core.exception.seat;

import com.tixy.core.exception.BusinessException;
import com.tixy.core.exception.ErrorCode;

public class SeatException extends BusinessException {
    public SeatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
