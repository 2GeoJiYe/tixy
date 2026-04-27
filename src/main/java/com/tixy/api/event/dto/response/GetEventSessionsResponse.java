package com.tixy.api.event.dto.response;

import java.time.LocalDateTime;

public record GetEventSessionsResponse (
        Long sessionId,
        String eventTitle,
        Long sessionSeatCount,
        String eventSessionStatus,
        LocalDateTime sessionOpenDate,
        LocalDateTime sessionCloseDate,
        Long minPrice,
        Long maxPrice
){
}
