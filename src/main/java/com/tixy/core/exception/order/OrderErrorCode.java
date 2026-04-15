package com.tixy.core.exception.order;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    WALLET_ADDRESS_NO_EXIST(HttpStatus.BAD_REQUEST, "W001", "유저가 소유한 지갑이 없습니다."),
    CREAT_ORDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "W002", "주문생성 중 에러가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}