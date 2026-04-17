package com.tixy.api.seat.service;

import com.tixy.api.event.entity.Event;
import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.seat.dto.response.SeatHoldResponse;
import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.service.TicketTypeService;
import com.tixy.core.security.annotation.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @RedisLock(key = SEAT_HOLD_PREFIX, idx = 1,timeout = 10)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SeatHoldResponse seatHold(Long eventSessionId, List<Long> seatIds, Long userId) {
        List<String> seatLabels = new ArrayList<>();
        eventSessionService.checkSessionSaleOpen(eventSessionId);
        List<SeatSession> seatSessions = seatSessionService.getSeatSessions(eventSessionId, seatIds);
        for (SeatSession seatSession : seatSessions) {
            seatSession.setHeld(userId);
            seatLabels.add(seatSession.getSeat().getRowLabel());
        }

        Seat seat = seatService.getBySeatId(seatIds.get(0));
        TicketType ticketType =  ticketTypeService.getTicketTypeByEventSessionId(eventSessionId ,seat.getSeatSection().getId());
        EventSession eventSession = ticketType.getEventSession();
        Event event = eventSession.getEvent();

        return new  SeatHoldResponse(
                seatLabels,
                seatSessions,
                ticketType,
                event.getTitle(),
                ticketType.getSeatSection().getSectionName(),
                eventSession.getSessionOpenDate(),
                eventSession.getSessionCloseDate()
                );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SeatHoldResponse seatPessimisticHold(Long eventSessionId, List<Long> seatIds, Long userId) {
        List<String> seatLabels = new ArrayList<>();
        eventSessionService.checkSessionSaleOpen(eventSessionId);
        List<SeatSession> seatSessions = new ArrayList<>();
        for (Long seatId : seatIds) {
            SeatSession seatSession = seatSessionService.getSeatSessionWithLock(eventSessionId, seatId);
            seatSession.setHeld(userId);
            seatSessions.add(seatSession);
            seatLabels.add(seatSession.getSeat().getRowLabel());
        }

        Seat seat = seatService.getBySeatId(seatIds.get(0));
        TicketType ticketType =  ticketTypeService.getTicketTypeByEventSessionId(eventSessionId ,seat.getSeatSection().getId());
        EventSession eventSession = ticketType.getEventSession();
        Event event = eventSession.getEvent();
        return new  SeatHoldResponse(
                seatLabels,
                seatSessions,
                ticketType,
                event.getTitle(),
                ticketType.getSeatSection().getSectionName(),
                eventSession.getSessionOpenDate(),
                eventSession.getSessionCloseDate()
        );
    }

    @Transactional
    public void seatHoldNoLock(Long eventSessionId, List<Long> seatIds, Long userId) {
        eventSessionService.checkSessionSaleOpen(eventSessionId);
        List<SeatSession> seatSessions = seatSessionService.getSeatSessions(eventSessionId, seatIds);
        for (SeatSession seatSession : seatSessions) {
            seatSession.setHeld(userId);
        }
    }

    @RedisLock(key = SEAT_HOLD_PREFIX, idx = 1,timeout = 10) // 똑같이 락은 걸어두어야 한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseSeatHold(Long eventSessionId, List<Long> seatIds){
        List<SeatSession> seatSessions = seatSessionService.getSeatSessions(eventSessionId, seatIds);

        for (SeatSession seatSession : seatSessions) {
            seatSession.unHeld();
        }
    }
}
