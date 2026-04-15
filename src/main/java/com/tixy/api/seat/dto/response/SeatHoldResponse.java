package com.tixy.api.seat.dto.response;

import com.tixy.api.ticket.entity.TicketType;

import java.time.LocalDateTime;
import java.util.List;

public record SeatHoldResponse(
        List<String> seatLabels,
        TicketType ticketType,
        String eventTitle,
        String seatSectionName,
        LocalDateTime sessionOpenDatetime,
        LocalDateTime sessionEndDatetime
) {
}
