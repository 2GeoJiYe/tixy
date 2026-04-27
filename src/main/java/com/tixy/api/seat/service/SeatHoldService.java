package com.tixy.api.seat.service;

import com.tixy.api.event.entity.Event;
import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.seat.dto.response.SeatHoldResponse;
import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.service.TicketTypeService;
import com.tixy.core.annotation.MemberWallet;
import com.tixy.core.exception.seat.SeatException;
import com.tixy.core.security.annotation.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.tixy.core.exception.seat.SeatErrorCode.SEAT_SESSION_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatHoldService {
    private static final String SEAT_HOLD_PREFIX = "seat-hold:";

    private final SeatSessionService seatSessionService;
    private final EventSessionService eventSessionService;
    private final TicketTypeService ticketTypeService;
    private final SeatService seatService;

//    K6 실측 결과 성공 요청 기준 평균 190ms가 소요되었으며, 3초는 실제 수행 시간 대비 약 15배의 여유를 둔 값입니다.
//    평균 190ms 기준으로 15배 여유를 둔 이유는,네트워크 지연, GC pause, DB 부하 등 예외 상황을 고려하였습니다
    @RedisLock(key = SEAT_HOLD_PREFIX, idx = 1,timeout = 3)
    @Transactional
    @MemberWallet(idx = 3)
    public SeatHoldResponse seatHold(Long eventSessionId, List<Long> seatIds,Long seatSectionId, Long memberId) {
        List<String> seatLabels = new ArrayList<>();

        eventSessionService.checkSessionSaleOpen(eventSessionId, seatSectionId);
        List<SeatSession> seatSessions = seatSessionService.getSeatSessions(eventSessionId, seatIds);
        for (SeatSession seatSession : seatSessions) {
            seatSession.setHeld(memberId);
            seatLabels.add(seatSession.getSeat().getRowLabel());
        }
        Seat seat = seatService.getBySeatId(seatIds.get(0));
        TicketType ticketType =  ticketTypeService.getTicketTypeByEventSessionId(eventSessionId ,seat.getSeatSection().getId());
        EventSession eventSession = ticketType.getEventSession();
        Event event = eventSession.getEvent();

        return new  SeatHoldResponse(
                seatLabels,
                ticketType.getId(),
                event.getTitle(),
                ticketType.getSeatSection().getSectionName(),
                eventSession.getSessionOpenDate(),
                eventSession.getSessionCloseDate()
                );
    }

    @Transactional
    @MemberWallet(idx = 3)
    public SeatHoldResponse seatPessimisticHold(Long eventSessionId, List<Long> seatIds,Long seatSectionId, Long memberId) {
        List<String> seatLabels = new ArrayList<>();
        eventSessionService.checkSessionSaleOpen(eventSessionId,seatSectionId);
        for (Long seatId : seatIds) {
            SeatSession seatSession = seatSessionService.getSeatSessionWithLock(eventSessionId, seatId);
            seatSession.setHeld(memberId);
            seatLabels.add(seatSession.getSeat().getRowLabel());
        }

        Seat seat = seatService.getBySeatId(seatIds.get(0));
        TicketType ticketType =  ticketTypeService.getTicketTypeByEventSessionId(eventSessionId ,seat.getSeatSection().getId());
        EventSession eventSession = ticketType.getEventSession();
        Event event = eventSession.getEvent();
        return new  SeatHoldResponse(
                seatLabels,
                ticketType.getId(),
                event.getTitle(),
                ticketType.getSeatSection().getSectionName(),
                eventSession.getSessionOpenDate(),
                eventSession.getSessionCloseDate()
        );
    }

    @Transactional
    @MemberWallet(idx = 3)
    public SeatHoldResponse seatHoldNoLock(Long eventSessionId, List<Long> seatIds,Long seatSectionId, Long memberId) {
        List<String> seatLabels = new ArrayList<>();
        eventSessionService.checkSessionSaleOpen(eventSessionId,seatSectionId);
        List<SeatSession> seatSessions = seatSessionService.getSeatSessions(eventSessionId, seatIds);
        for (SeatSession seatSession : seatSessions) {
            seatSession.setHeld(memberId);
            seatLabels.add(seatSession.getSeat().getRowLabel());
        }

        Seat seat = seatService.getBySeatId(seatIds.get(0));
        TicketType ticketType =  ticketTypeService.getTicketTypeByEventSessionId(eventSessionId ,seat.getSeatSection().getId());
        EventSession eventSession = ticketType.getEventSession();
        Event event = eventSession.getEvent();

        return new  SeatHoldResponse(
                seatLabels,
                ticketType.getId(),
                event.getTitle(),
                ticketType.getSeatSection().getSectionName(),
                eventSession.getSessionOpenDate(),
                eventSession.getSessionCloseDate()
        );
    }

    @RedisLock(key = SEAT_HOLD_PREFIX, idx = 1,timeout = 3) // 똑같이 락은 걸어두어야 한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseSeatHold(Long eventSessionId, List<Long> seatIds){
        List<SeatSession> seatSessions = seatSessionService.getSeatSessions(eventSessionId, seatIds);

        for (SeatSession seatSession : seatSessions) {
            seatSession.unHeld();
        }
    }

    public void checkMember(Long eventSessionId, List<Long> seatIds, Long memberId) {
        List<SeatSession> seatSessions = seatSessionService.getSeatSessions(eventSessionId, seatIds);
        for (SeatSession seatSession : seatSessions) {
            if(!memberId.equals(seatSession.getUserId())){
                throw new SeatException(SEAT_SESSION_USER_NOT_FOUND);
            }
        }
    }
}
