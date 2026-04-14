package com.tixy.api.seat.service;

import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.order.dto.request.OrderRequest;
import com.tixy.api.order.service.OrderService;
import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.service.TicketTypeService;
import com.tixy.core.security.annotation.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatHoldService {
    private static final String SEAT_HOLD_PREFIX = "seat-hold:";

    private final SeatSessionService seatSessionService;
    private final EventSessionService eventSessionService;
    private final TicketTypeService ticketTypeService;
    private final SeatService seatService;
    private final OrderService orderService;

    @RedisLock(key = SEAT_HOLD_PREFIX, idx = 1,timeout = 10)
    @Transactional
    public void seatHold(Long eventSessionId, List<Long> seatIds, Long userId) {
        eventSessionService.checkSessionSaleOpen(eventSessionId);
        for (Long seatId : seatIds) {
            SeatSession seatSession = seatSessionService.getSeatSession(eventSessionId, seatId);
            seatSession.setHeld(userId);
        }
        // seatId 들이 같은 구역 내에서 구매를 보장 한다는 가정하에,
        // 하나의 seat를 조회해서 그 구역을 조회 해야한다.
        // 그 구역 정보와 이벤트 세션 정보로 tickettype을 조회한다.

        // 그리고 지금 이 주문생성은 별도의 트랜잭션으로 동작해도 될듯..?
        // 예약 선점에서 너무 오레 트랜잭션을 끌고가면 TTL만료 시간을 그만큰 길게 주어야하는데 그게 좋은가? 는 고민 해봐야함.
        Seat seat = seatService.getBySeatId(seatIds.get(0));
        TicketType ticketType = ticketTypeService.getTicketTypeByEventSessionId(eventSessionId ,seat.getSeatSection().getId());
        OrderRequest orderRequest = new OrderRequest(
                seatIds.size(),
                userId,
                ticketType
        );
        orderService.saveOrder(orderRequest);
    }

    @Transactional
    public void seatHoldNoLock(Long eventSessionId, List<Long> seatIds, Long userId) {
        eventSessionService.checkSessionSaleOpen(eventSessionId);

        for (Long seatId : seatIds) {
            SeatSession seatSession = seatSessionService.getSeatSession(eventSessionId, seatId);
            seatSession.setHeld(userId);
        }
    }
}
