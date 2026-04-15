package com.tixy.api.order.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.dto.response.CreateOrderResponse;
import com.tixy.api.seat.dto.response.SeatHoldResponse;
import com.tixy.api.seat.service.SeatHoldService;
import com.tixy.core.exception.order.OrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tixy.core.exception.order.OrderErrorCode.CREAT_ORDER_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeService {
    private final SeatHoldService seatHoldService;
    private final MemberService memberService;
    private final OrderService orderService;

    @Value("${payment.deposit-address}")
    private String depositAddress;

    @Transactional
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
            Long totalPrice = orderService.saveOrder(orderRequest);
            return new CreateOrderResponse(
                    totalPrice,
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
            throw new OrderException(CREAT_ORDER_FAILED);
        }
    }
}
