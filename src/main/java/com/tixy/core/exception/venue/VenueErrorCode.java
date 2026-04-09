package com.tixy.core.exception.venue;

import com.tixy.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VenueErrorCode implements ErrorCode {
    VENUE_NOT_FOUND(HttpStatus.BAD_REQUEST, "V001", "공연장 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
