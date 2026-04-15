package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.entity.Order;
import com.tixy.api.order.enums.OrderStatus;
import com.tixy.api.order.repository.OrderRepository;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.core.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String ORDER_PREFIX = "ODR:";
    private final OrderRepository orderRepository;


    public Long saveOrder(OrderRequest orderRequest){
        TicketType ticketType = orderRequest.ticketType();
        Member member = orderRequest.member();

        Long totalPrice = ticketType.getPrice() * orderRequest.ticketCount();
        Order order = Order.builder()
                .orderNo(PublicIdGenerator.generate(ORDER_PREFIX))
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .member(member)
                .ticketType(ticketType)
                .paidWalletAddress(member.getWalletAddress())
                .build();

        orderRepository.save(order);

        return totalPrice;
    }
}
