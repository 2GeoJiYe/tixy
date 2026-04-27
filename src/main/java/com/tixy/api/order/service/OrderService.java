package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.order.dto.request.CreateOrderRequest;
import com.tixy.api.order.entity.Order;
import com.tixy.api.order.enums.OrderStatus;
import com.tixy.api.order.repository.OrderRepository;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.core.exception.order.OrderErrorCode;
import com.tixy.core.exception.order.OrderException;
import com.tixy.core.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String ORDER_PREFIX = "ODR";
    private final OrderRepository orderRepository;


    public Order saveOrder(CreateOrderRequest createOrderRequest){
        TicketType ticketType = createOrderRequest.ticketType();
        Member member = createOrderRequest.member();
        checkDuplicateOrderRequest(member, ticketType);
        Long totalPrice = ticketType.getPrice() * createOrderRequest.ticketCount();
        Order order = Order.builder()
                .ticketCount(createOrderRequest.ticketCount())
                .orderNo(PublicIdGenerator.generate(ORDER_PREFIX))
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .member(member)
                .ticketType(ticketType)
                .senderWalletAddress(member.getWalletAddress())
                .build();

        orderRepository.save(order);
        return order;
    }

    public Optional<Order> getOrderBySenderWalletAddress(String senderWalletAddress){
        return orderRepository.findPendingOrderByWalletAddress(senderWalletAddress);
    }

    public void checkDuplicateOrderRequest(Member member, TicketType ticketType){
        if(orderRepository.existsPendingTicket(member.getId() , ticketType.getId())){
            throw new OrderException(OrderErrorCode.DUPLICATE_ORDER_MEMBER);
        }
    }
}
