package com.tixy.api.dashboard.sales.dto.response;

public record GetSalesSummaryResponse(
        Long soldTicketCount,
        Long paidAmount,
        Integer paymentCount,
        Integer sessionCount
) {
}
