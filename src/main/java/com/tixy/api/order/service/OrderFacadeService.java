package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.api.order.dto.request.CreateOrderRequest;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.dto.response.CreateOrderResponse;
import com.tixy.api.order.dto.response.OrderResponse;
import com.tixy.api.seat.service.SeatHoldService;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeService {
    private final SeatHoldService seatHoldService;
    private final MemberService memberService;
    private final OrderService orderService;
    private final TicketTypeService ticketTypeService;

    @Value("${payment.deposit-address}")
    private String depositAddress;

    public CreateOrderResponse order(OrderRequest orderRequest, Long memberId){
        Member member = memberService.findById(memberId);
        member.checkMemberWallet();

        TicketType ticketType = ticketTypeService.getTicketTypeById(orderRequest.ticketTypeId());

        try {
            CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                    orderRequest.seatIds().size(),
                    member,
                    ticketType
            );
            OrderResponse orderResponse = orderService.saveOrder(createOrderRequest);
            return new CreateOrderResponse(
                    orderResponse.totalPrice(),
                    depositAddress,
                    orderRequest.seatIds().size()
            );
        }catch (Exception e){
            // 주문생성 실패 시 보상트랜잭션
            seatHoldService.releaseSeatHold(orderRequest.eventSessionId(), orderRequest.seatIds());
            log.error("주문생성 에러 발생 : {} ", e.getMessage());
            throw e;
        }
    }
}
