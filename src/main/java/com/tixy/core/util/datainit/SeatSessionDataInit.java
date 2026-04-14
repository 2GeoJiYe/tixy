package com.tixy.core.util.datainit;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.seat.enums.SeatStatus;
import com.tixy.api.seat.enums.SessionSeatStatus;
import com.tixy.api.seat.repository.SeatRepository;
import com.tixy.api.seat.repository.SeatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeatSessionDataInit {
    private final EventSessionRepository eventSessionRepository;
    private final SeatSessionRepository seatSessionRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public void initSeatSession() {
        if (seatSessionRepository.count() > 8) return;

        List<EventSession> eventSessions = eventSessionRepository.findAllByStatus(EventSessionStatus.ON_PERFORM);
        List<Seat> seats = seatRepository.findAllBySeatStatus(SeatStatus.ACTIVE);
        List<SeatSession> seatSessions = new ArrayList<>();

        for (EventSession eventSession : eventSessions) {
            for (Seat seat : seats) {
                SeatSession seatSession = SeatSession.builder()
                        .seat(seat)
                        .eventSession(eventSession)
                        .status(SessionSeatStatus.AVAILABLE)
                        .build();
                seatSessions.add(seatSession);
                seatSessionRepository.save(seatSession);
            }
        }

        seatSessionRepository.saveAll(seatSessions);
        System.out.println("seatSession dummy data " + seatSessions.size() + "개 저장 완료!");
    }
}
