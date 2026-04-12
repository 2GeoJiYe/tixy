package com.tixy.core.util.datainit;

import com.tixy.api.event.entity.Event;
import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.event.repository.EventSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
public class EventSessionDataInit {
    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;

    @Transactional
    public void initEventSessions() {
        if (eventSessionRepository.count() > 0) return;

        List<Event> events = eventRepository.findAll();
        Random random = new Random(42);
        List<EventSession> sessions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Event event : events) {
            LocalDateTime eventStart = event.getOpenDate();                    // 첫 세션 시작
            LocalDateTime eventEnd = event.getEndDate().minusDays(1);         // 마지막 세션 종료
            long totalDays = ChronoUnit.DAYS.between(eventStart, eventEnd);

            int sessionCount = 3 + random.nextInt(8); // 3~10개

            // 첫날/마지막날 고정, 나머지 랜덤으로 날짜 뽑기
            Set<Long> offsetSet = new TreeSet<>();
            offsetSet.add(0L);                  // 첫날
            offsetSet.add(totalDays);           // 마지막날
            while (offsetSet.size() < sessionCount) {
                offsetSet.add((long) (random.nextDouble() * (totalDays - 1)) + 1);
            }
            List<Long> offsets = new ArrayList<>(offsetSet);

            for (int i = 0; i < offsets.size(); i++) {
                LocalDateTime sessionOpen = eventStart.plusDays(offsets.get(i))
                        .withHour(random.nextInt(24))
                        .withMinute(0);

                // 세션 길이: 70% → 1~3일, 30% → 4~7일 (최소 5시간)
                long durationHours;
                if (random.nextDouble() < 0.7) {
                    durationHours = 5 + (long) (random.nextDouble() * (72 - 5));   // 5h ~ 3일
                } else {
                    durationHours = 72 + (long) (random.nextDouble() * (168 - 72)); // 3일 ~ 7일
                }
                LocalDateTime sessionClose = sessionOpen.plusHours(durationHours);

                // status 현재 시각 기준
                EventSessionStatus status;
                if (now.isBefore(sessionOpen)) {
                    status = EventSessionStatus.SCHEDULED;
                } else if (now.isBefore(sessionClose)) {
                    status = EventSessionStatus.ON_PERFORM;
                } else {
                    status = EventSessionStatus.CLOSED;
                }

                EventSession session = EventSession.builder()
                        .event(event)
                        .session((i + 1) + "회차")
                        .status(status)
                        .sessionOpenDate(sessionOpen)
                        .sessionCloseDate(sessionClose)
                        .build();

                sessions.add(session);
            }
        }

        eventSessionRepository.saveAll(sessions);
        System.out.println("eventSession dummy data " + sessions.size() + "개 저장 완료!");
    }
}
