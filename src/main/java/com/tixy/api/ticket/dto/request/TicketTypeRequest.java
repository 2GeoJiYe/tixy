package com.tixy.api.ticket.dto.request;

public record TicketTypeRequest(
        Long seatSectionId,
        Long price
) {}