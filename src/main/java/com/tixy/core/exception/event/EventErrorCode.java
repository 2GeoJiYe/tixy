package com.tixy.core.exception.event;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {
    EVENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "E001", "공연정보를 찾을 수 없습니다."),
    EVENT_SESSION_NOT_SALE(HttpStatus.BAD_REQUEST, "E002", "판매 중인 공연이 아닙니다."),
    EVENT_SESSION_CLOSED(HttpStatus.BAD_REQUEST, "E003", "종료된 공연입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
