package com.tixy.core.exception.ticket;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TicketTypeErrorCode implements ErrorCode {
    TICKET_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "티켓 마스터 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
