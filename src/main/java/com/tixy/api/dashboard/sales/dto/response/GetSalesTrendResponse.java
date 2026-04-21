package com.tixy.api.dashboard.sales.dto.response;

public record GetSalesTrendResponse(
        String bucket,
        Long soldTicketCount,
        Long paidAmount,
        Integer paymentCount
) {
}
