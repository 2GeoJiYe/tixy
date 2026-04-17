package com.tixy.core.exception.payment;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    DUPLICATE_PAYMENT(HttpStatus.BAD_REQUEST, "P001", "중복된 결제정보 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}