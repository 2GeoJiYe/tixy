package com.tixy.core.exception.payment;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    DUPLICATE_PAYMENT(HttpStatus.BAD_REQUEST, "P001", "중복된 결제정보 요청입니다."),
    NOT_MATCH_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "P002", "실 결제금액이 올바르지 않습니다."),
    MATCHED_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "요청된 지갑 주소를 소유한 고객이 없습니다."),
    MATCHED_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "P004", "요청된 주문 정볼를 찾을 수없습니다."),
    EXPIRE_PAYMENT(HttpStatus.BAD_REQUEST, "P005", "결제 가능 시간이 만료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}