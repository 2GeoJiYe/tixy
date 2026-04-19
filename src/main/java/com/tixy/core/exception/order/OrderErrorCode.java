package com.tixy.core.exception.order;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    WALLET_ADDRESS_NO_EXIST(HttpStatus.BAD_REQUEST, "O001", "유저가 소유한 지갑이 없습니다."),
    CREAT_ORDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O002", "주문생성 중 에러가 발생했습니다."),
    NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, "O003", "지갑정보로 주문정보를 찾을 수 없습니다."),
    DUPLICATE_ORDER_MEMBER(HttpStatus.BAD_REQUEST, "O004", "동일한 고객이 이미 해당 공연에 주문 내역이있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}