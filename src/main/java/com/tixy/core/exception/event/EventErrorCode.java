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
    EVENT_SESSION_CLOSED(HttpStatus.BAD_REQUEST, "E003", "종료된 공연입니다."),
    RESERVATION_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "E004", "예매가 시작된 이벤트는 수정할 수 없습니다."),
    INVALID_EVENT_DATE(HttpStatus.BAD_REQUEST, "E005", "이벤트 날짜 오류"),
    EVENT_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "E006", "수정 및 삭제가 불가능한 이벤트 상태입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
