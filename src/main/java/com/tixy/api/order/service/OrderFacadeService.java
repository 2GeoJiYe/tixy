package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.dto.response.CreateOrderResponse;
import com.tixy.api.order.dto.response.OrderResponse;
import com.tixy.api.seat.dto.response.SeatHoldResponse;
import com.tixy.api.seat.service.SeatHoldService;
import com.tixy.api.seat.service.SeatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeService {
    private final SeatHoldService seatHoldService;
    private final MemberService memberService;
    private final OrderService orderService;
    private final SeatSessionService seatSessionService;

    @Value("${payment.deposit-address}")
    private String depositAddress;

    public CreateOrderResponse order(Long eventSessionId, List<Long> seatIds, Long memberId){
        Member member = memberService.findById(memberId);
        member.checkMemberWallet();
        SeatHoldResponse seatHoldResponse = seatHoldService.seatHold(eventSessionId, seatIds, memberId);
        try {
            OrderRequest orderRequest = new OrderRequest(
                    seatIds.size(),
                    member,
                    seatHoldResponse.ticketType()
            );
            OrderResponse orderResponse = orderService.saveOrder(orderRequest);
            // seat session에 주문 정보 저장
            seatSessionService.setOrderToSeatSession(seatHoldResponse.seatSessions(), orderResponse.order());
            return new CreateOrderResponse(
                    orderResponse.totalPrice(),
                    depositAddress,
                    seatHoldResponse.eventTitle(), // seatHoldResponse 가 해당 트랜잭션 외부에서 만들어져서 lazy로딩 에러 발생..
                    seatHoldResponse.seatSectionName(),
                    seatHoldResponse.seatLabels(),
                    seatHoldResponse.seatLabels().size(),
                    seatHoldResponse.sessionOpenDatetime(),
                    seatHoldResponse.sessionEndDatetime()
            );
        }catch (Exception e){
            // 주문생성 실패 시 보상트랜잭션
            seatHoldService.releaseSeatHold(eventSessionId, seatIds);
            log.error("주문생성 에러 발생 : {} ", e.getMessage());
            throw e;
        }
    }

    public CreateOrderResponse orderPessimistic(Long eventSessionId, List<Long> seatIds, Long memberId){
        Member member = memberService.findById(memberId);
        member.checkMemberWallet();
        SeatHoldResponse seatHoldResponse = seatHoldService.seatPessimisticHold(eventSessionId, seatIds, memberId);
        try {
            OrderRequest orderRequest = new OrderRequest(
                    seatIds.size(),
                    member,
                    seatHoldResponse.ticketType()
            );
            OrderResponse orderResponse = orderService.saveOrder(orderRequest);
            return new CreateOrderResponse(
                    orderResponse.totalPrice(),
                    depositAddress,
                    seatHoldResponse.eventTitle(), // seatHoldResponse 가 해당 트랜잭션 외부에서 만들어져서 lazy로딩 에러 발생..
                    seatHoldResponse.seatSectionName(),
                    seatHoldResponse.seatLabels(),
                    seatHoldResponse.seatLabels().size(),
                    seatHoldResponse.sessionOpenDatetime(),
                    seatHoldResponse.sessionEndDatetime()
            );
        }catch (Exception e){
            // 주문생성 실패 시 보상트랜잭션
            seatHoldService.releaseSeatHold(eventSessionId, seatIds);
            log.error("주문생성 에러 발생 : {} ", e.getMessage());
            throw e;
        }
    }
}
