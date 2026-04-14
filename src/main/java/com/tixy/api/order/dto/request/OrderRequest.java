package com.tixy.api.order.dto.request;

import com.tixy.api.ticket.entity.TicketType;

public record OrderRequest(
        int ticketCount,
        Long memberId,
        TicketType ticketType
) {
}
