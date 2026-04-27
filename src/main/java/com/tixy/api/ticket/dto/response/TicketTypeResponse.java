package com.tixy.api.ticket.dto.response;

import com.tixy.api.seat.enums.Grade;

public record TicketTypeResponse(
        Long sessionId,
        Grade grade,
        Long price
){
}