package com.tixy.api.ticket.dto.response;

import java.util.List;

public record CreateTicketTypeResponse(
        List<TicketTypeResponse> ticketTypeResponses
) {
}
