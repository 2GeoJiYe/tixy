package com.tixy.core.exception.order;

import com.tixy.core.exception.BusinessException;
import com.tixy.core.exception.ErrorCode;

public class OrderException extends BusinessException {
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
