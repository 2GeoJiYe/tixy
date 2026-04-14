package com.tixy.api.event.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record GetOneEventSessionResponse(
        String eventTitle,
        Long sessionSeatCount,
        String eventSessionStatus,
        LocalDateTime sessionOpenDate,
        LocalDateTime sessionCloseDate,
        LocalDateTime saleOpenDate,
        LocalDateTime saleCloseDate,
        Map<String, Long> ticketTypePrice
) {
}
