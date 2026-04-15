package com.tixy.api.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderResponse(
        Long totalPayment,
        String depositAddress,
        String eventName,
        String seatSectionName,
        List<String> seatLabels,
        int ticketCount,
        LocalDateTime eventSessionStartDateTime,
        LocalDateTime eventSessionEndDateTime
) {
}
