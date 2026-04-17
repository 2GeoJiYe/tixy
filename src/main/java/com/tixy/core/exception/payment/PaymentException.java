package com.tixy.core.exception.payment;

import com.tixy.core.exception.BusinessException;
import com.tixy.core.exception.ErrorCode;

public class PaymentException extends BusinessException {
    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
