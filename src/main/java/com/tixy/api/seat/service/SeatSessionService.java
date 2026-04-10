package com.tixy.api.seat.service;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.enums.SessionSeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatSessionService {
    private final JdbcTemplate jdbcTemplate;
    private final SeatSectionService seatSectionService;
    private final EventSessionService eventSessionService;
    private final SeatService seatService;

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
                "INSERT INTO seat_sessions (seat_id, event_session_id, status) VALUES (?, ?, ?)",
                seatIds,
                seatIds.size(),
                (ps, item) -> {
                    ps.setLong(1, item);
                    ps.setLong(2, eventSession);
                    ps.setString(3, SessionSeatStatus.AVAILABLE.name());
                }
        );
    }
}
