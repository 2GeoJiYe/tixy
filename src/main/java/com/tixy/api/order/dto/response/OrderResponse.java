package com.tixy.api.order.dto.response;

import com.tixy.api.order.entity.Order;

public record OrderResponse (
        Long totalPrice,
        Order order
) {
}
