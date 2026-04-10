package com.tixy.api.ticket.dto.request;


import java.util.List;

public record CreateTicketTypeRequest(
        Long eventSessionId,
        List<TicketTypeRequest> ticketTypeInfos
) {
}
