package com.tixy.core.exception.ticket;

import com.tixy.core.exception.BusinessException;
import com.tixy.core.exception.ErrorCode;

public class TicketTypeException extends BusinessException {
    public TicketTypeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
