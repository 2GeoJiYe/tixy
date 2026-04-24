package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.api.order.dto.request.CreateOrderRequest;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.dto.response.CreateOrderResponse;
import com.tixy.api.order.entity.Order;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.seat.service.SeatHoldService;
import com.tixy.api.seat.service.SeatSessionService;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.service.TicketTypeService;
import com.tixy.core.annotation.MemberWallet;
import com.tixy.core.util.ExchangeRateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeService {
    private final SeatHoldService seatHoldService;
    private final MemberService memberService;
    private final OrderService orderService;
    private final TicketTypeService ticketTypeService;
    private final SeatSessionService seatSessionService;
    private final ExchangeRateUtils exchangeRateUtils;

    @Value("${payment.deposit-address}")
    private String depositAddress;

    @MemberWallet(idx = 1)
    @Transactional
    public CreateOrderResponse order(OrderRequest orderRequest, Long memberId){
        Member member = memberService.findById(memberId);
        try {
            seatHoldService.checkMember(orderRequest.eventSessionId(),orderRequest.seatIds(),memberId);

            TicketType ticketType = ticketTypeService.getTicketTypeById(orderRequest.ticketTypeId());

            CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                    orderRequest.seatIds().size(),
                    member,
                    ticketType
            );
            Order order = orderService.saveOrder(createOrderRequest);

            List<SeatSession> sessionList = seatSessionService.getSeatSessions(orderRequest.eventSessionId(),orderRequest.seatIds());
            sessionList.forEach(seatSession -> {
                seatSession.setOrder(order);
            });

            BigDecimal totalInUsdt = exchangeRateUtils.convertKrwToUsdt(order.getTotalPrice());

            return new CreateOrderResponse(
                    order.getTotalPrice(),
                    totalInUsdt,
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
