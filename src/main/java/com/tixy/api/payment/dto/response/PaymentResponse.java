package com.tixy.api.payment.dto.response;

import com.tixy.api.ticket.dto.response.CreateTicketResponse;

import java.util.List;

public record PaymentResponse(
        String WalletAddress,
        String MemberEmail,
        List<CreateTicketResponse> ticketInfos
) {
}
