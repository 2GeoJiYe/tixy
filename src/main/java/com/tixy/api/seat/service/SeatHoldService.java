package com.tixy.api.seat.service;

import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.seat.entity.SeatSession;
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

    @RedisLock(key = SEAT_HOLD_PREFIX, idx = 1,timeout = 10)
    @Transactional
    public void seatHold(Long eventSessionId, List<Long> seatIds, Long userId) {
        eventSessionService.checkSessionSaleOpen(eventSessionId);
        for (Long seatId : seatIds) {
            SeatSession seatSession = seatSessionService.getSeatSession(eventSessionId, seatId);
            seatSession.setHeld(userId);
        }
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
