package com.tixy.api.order.dto.response;

public record CreateOrderResponse(
        Long totalPayment,
        String depositAddress,
        int ticketCount
) {
}
