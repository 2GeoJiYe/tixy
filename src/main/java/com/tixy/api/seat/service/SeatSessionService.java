package com.tixy.api.seat.service;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.seat.enums.SessionSeatStatus;
import com.tixy.api.seat.repository.SeatSessionRepository;
import com.tixy.core.exception.seat.SeatErrorCode;
import com.tixy.core.exception.seat.SeatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatSessionService {
    private final JdbcTemplate jdbcTemplate;
    private final SeatSectionService seatSectionService;
    private final EventSessionService eventSessionService;
    private final SeatService seatService;
    private final SeatSessionRepository seatSessionRepository;

    @Async
    @Transactional
    public void createSeatSessions(Long eventId) {
        // 이벤트 회차 정보 다 끌어오기
        List<EventSession> eventSessions = eventSessionService.getAllByEventId(eventId);
        // 끌어온 이벤트의 공연장 id
        Long venueId = eventSessions.get(0).getEvent().getVenue().getId();

        // 공연장 id로 구역 싹 가져오기
        List<SeatSection> seatSections = seatSectionService.getAllByVenueId(venueId);

        // 구역별로
        seatSections.forEach(section -> {
            // 죄석 정보 가져와서
            List<Long> seatIds = seatService.getAllBySectionId(section.getId())
                    .stream()
                    .map(Seat::getId)
                    .toList();

            // 회차별로 죄석 정보 넣어버리기
            if (!seatIds.isEmpty()) {
                eventSessions.forEach(session ->
                        insertSeatSession(session.getId(), seatIds)
                );
            }
        });
    }

    private void insertSeatSession(Long eventSession, List<Long> seatIds) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO seat_sessions (seat_id, event_session_id, status, user_id, expire_at) VALUES (?, ?, ?, ?, ?)",
                seatIds,
                seatIds.size(),
                (ps, item) -> {
                    ps.setLong(1, item);
                    ps.setLong(2, eventSession);
                    ps.setString(3, SessionSeatStatus.AVAILABLE.name());
                    ps.setNull(4, Types.BIGINT);
                    ps.setNull(5, Types.TIMESTAMP);
                }
        );
    }

    public List<SeatSession> getSeatSessions(Long eventSessionId, List<Long> seatId) {
        return seatSessionRepository.findByEventSessionIdAndSeatId(eventSessionId, seatId);
    }

    public SeatSession getSeatSessionWithLock(Long eventSessionId, Long seatId) {
        return seatSessionRepository.findByEventSessionLock(eventSessionId, seatId).orElseThrow(
                () -> new SeatException(SeatErrorCode.SEAT_SESSION_NOT_FOUND)
        );
    }
}
