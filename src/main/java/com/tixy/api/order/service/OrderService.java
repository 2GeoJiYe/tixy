package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.entity.Order;
import com.tixy.api.order.enums.OrderStatus;
import com.tixy.api.order.repository.OrderRepository;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.service.TicketTypeService;
import com.tixy.core.exception.order.OrderException;
import com.tixy.core.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tixy.core.exception.order.OrderErrorCode.WALLET_ADDRESS_NO_EXIST;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String ORDER_PREFIX = "ODR:";
    private final OrderRepository orderRepository;
    private final TicketTypeService ticketTypeService;
    private final MemberService memberService;


    @Transactional
    public void saveOrder(OrderRequest orderRequest){
        Member member = memberService.findById(orderRequest.memberId());
        if(member.getWalletAddress().isBlank()){ // 지갑 주소 없으면 주문 XX
            throw new OrderException(WALLET_ADDRESS_NO_EXIST);
        }

        TicketType ticketType = orderRequest.ticketType();

        Order order = Order.builder()
                .orderNo(PublicIdGenerator.generate(ORDER_PREFIX))
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(ticketType.getPrice() * orderRequest.ticketCount())
                .member(member)
                .ticketType(ticketType)
                .paidWalletAddress(member.getWalletAddress())
                .build();

        orderRepository.save(order);
    }
}
