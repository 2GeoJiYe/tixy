package com.tixy.api.seat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SeatHoldResponse(
        List<String> seatLabels,
        Long ticketTypeId,
        String eventTitle,
        String seatSectionName,
        LocalDateTime sessionOpenDatetime,
        LocalDateTime sessionEndDatetime
) {
}
