package com.tixy.api.order.dto.request;

import com.tixy.api.member.entity.Member;
import com.tixy.api.ticket.entity.TicketType;

public record OrderRequest(
        int ticketCount,
        Member member,
        TicketType ticketType
) {
}
