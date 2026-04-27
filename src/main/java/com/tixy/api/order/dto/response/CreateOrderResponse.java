package com.tixy.api.order.dto.response;

import java.math.BigDecimal;

public record CreateOrderResponse(
        Long totalPayment,
        BigDecimal usdAmount,
        String depositAddress,
        int ticketCount
) {
}
