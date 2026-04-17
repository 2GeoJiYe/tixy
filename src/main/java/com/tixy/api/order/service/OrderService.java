package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.dto.response.OrderResponse;
import com.tixy.api.order.entity.Order;
import com.tixy.api.order.enums.OrderStatus;
import com.tixy.api.order.repository.OrderRepository;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.core.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String ORDER_PREFIX = "ODR";
    private final OrderRepository orderRepository;


    public OrderResponse saveOrder(OrderRequest orderRequest){
        TicketType ticketType = orderRequest.ticketType();
        Member member = orderRequest.member();

        Long totalPrice = ticketType.getPrice() * orderRequest.ticketCount();
        Order order = Order.builder()
                .ticketCount(orderRequest.ticketCount())
                .orderNo(PublicIdGenerator.generate(ORDER_PREFIX))
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .member(member)
                .ticketType(ticketType)
                .senderWalletAddress(member.getWalletAddress())
                .build();

        orderRepository.save(order);

        return new OrderResponse(
                totalPrice,
                order
        );
    }

    public Optional<Order> getOrderBySenderWalletAddress(String senderWalletAddress){
        return orderRepository.findPendingOrderByWalletAddress(senderWalletAddress);
    }
}
