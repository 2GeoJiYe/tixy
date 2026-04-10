package com.tixy.core.util.datainit;

import com.tixy.api.event.entity.Event;
import com.tixy.api.event.enums.EventStatus;
import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class EventDataInit {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final JdbcTemplate jdbcTemplate; // 이후에 event data 개수가 늘어날 때를 대비

    @Transactional
    public void initEvents() {
        if (eventRepository.count() > 0) return;

        List<Venue> venues = venueRepository.findAll();
        Random random = new Random(42);

        // 500개 이벤트 타이틀 소재
        List<String> artists = List.of(
                "BTS", "아이유", "블랙핑크", "세븐틴", "NCT", "에스파", "뉴진스",
                "임영웅", "나훈아", "이선희", "조용필", "박효신", "태연", "규현",
                "콜드플레이", "에드 시런", "브루노 마스", "레이디 가가", "마룬5", "빌리 아일리시"
        );
        List<String> suffixes = List.of(
                "월드투어 서울", "콘서트", "팬미팅", "단독 공연", "연말 콘서트",
                "스페셜 라이브", "갈라쇼", "기념 공연", "투어", "앙코르 콘서트"
        );

        // 날짜 범위: 2025-01-01 ~ 2026-12-31
        LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 1, 0, 0);
        long rangeDays = ChronoUnit.DAYS.between(rangeStart, LocalDateTime.of(2026, 12, 31, 0, 0));

        // 상태 비율 3(SCHEDULED):3(OPEN):4(CLOSED) → 150:150:200
        List<EventStatus> statusPool = new ArrayList<>();
        for (int i = 0; i < 150; i++) statusPool.add(EventStatus.SCHEDULED);
        for (int i = 0; i < 150; i++) statusPool.add(EventStatus.OPEN);
        for (int i = 0; i < 200; i++) statusPool.add(EventStatus.CLOSED);
        Collections.shuffle(statusPool, random);

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Venue venue = venues.get(random.nextInt(venues.size()));
            String title = artists.get(random.nextInt(artists.size()))
                    + " " + suffixes.get(random.nextInt(suffixes.size()));
            String description = title + "의 공연입니다. 많은 관람 부탁드립니다.";
            EventStatus status = statusPool.get(i);

            // openDate: 2022~2026 범위 내 랜덤
            long offsetDays = (long) (random.nextDouble() * rangeDays);
            LocalDateTime openDate = rangeStart.plusDays(offsetDays)
                    .withHour(random.nextInt(24))
                    .withMinute(random.nextBoolean() ? 0 : 30);

            // endDate: 1일 ~ 30일 후
            int durationDays = 1 + random.nextInt(14);
            LocalDateTime endDate = openDate.plusDays(durationDays);

            Event event = Event.builder()
                    .venue(venue)
                    .title(title)
                    .description(description)
                    .eventStatus(status)
                    .openDate(openDate)
                    .endDate(endDate)
                    .build();

            events.add(event);
        }

        eventRepository.saveAll(events); // 이후 데이터 많이 넣어야하면 주석처리 하고 밑에꺼 풀어주기
        System.out.println("event data 500개 저장 완료!");

//        String sql = """
//            INSERT INTO event (venue_id, title, description, event_status, open_date, end_date)
//            VALUES (?, ?, ?, ?, ?, ?)
//            """;
//
//        List<Object[]> batchArgs = events.stream()
//                .map(e -> new Object[]{
//                        e.getVenue().getId(),
//                        e.getTitle(),
//                        e.getDescription(),
//                        e.getEventStatus().name(),
//                        e.getOpenDate(),
//                        e.getEndDate()
//                })
//                .toList();
//
//        jdbcTemplate.batchUpdate(sql, batchArgs);
//        System.out.println("테스트용 Event 500개 bulk insert 완료!");
    }
}
