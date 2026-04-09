package com.tixy.api.event.enums;

import lombok.Getter;

@Getter
public enum EventSessionStatus {
    SCHEDULED("오픈 예정"),
    CLOSED("종료");
    private final String status;

    EventSessionStatus(String status) {
        this.status = status;
    }
}
