package com.tixy.core.exception.seat;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatErrorCode implements ErrorCode {
    SEAT_SECTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "S001", "공연장 구역 정보를 찾을 수 없습니다."),
    SEAT_DUPLICATE(HttpStatus.BAD_REQUEST, "S002", "이미 존재하는 좌석입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
