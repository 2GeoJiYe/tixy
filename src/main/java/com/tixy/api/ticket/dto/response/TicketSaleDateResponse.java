package com.tixy.api.ticket.dto.response;

import java.time.LocalDateTime;

public record TicketSaleDateResponse(
        LocalDateTime saleOpenDateTime,
        LocalDateTime saleCloseDateTime
) {
}
