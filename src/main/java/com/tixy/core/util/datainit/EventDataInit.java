package com.tixy.core.util.datainit;

import com.tixy.api.event.entity.Event;
import com.tixy.api.event.enums.EventStatus;
import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.enums.Category;
import com.tixy.api.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

        List<String> prefix1 = List.of("하얀", "복실", "까만", "행복한", "돼지");
        List<String> prefix2 = List.of("고양이", "강아지", "햄스터", "카피바라");
        List<String> alphabets = List.of("AA", "BB", "CC", "DD", "EE");

        // 알파벳 2개 순열 조합 (AA-BB, AA-CC ... 순서 다른거 포함)
        List<String> suffixes = new ArrayList<>();
        for (String a1 : alphabets) {
            for (String a2 : alphabets) {
                if (!a1.equals(a2)) suffixes.add(a1 + a2);
            }
        }

        // 300개 unique 조합 미리 생성
        List<String[]> combos = new ArrayList<>();
        for (String p1 : prefix1) {
            for (String p2 : prefix2) {
                for (String suffix : suffixes) {
                    combos.add(new String[]{p1, p2, suffix});
                }
            }
        }
        Collections.shuffle(combos, random);

        // 상태 비율 3:4:3
        List<EventStatus> statusPool = new ArrayList<>();
        for (int i = 0; i < 90; i++) statusPool.add(EventStatus.SCHEDULED);
        for (int i = 0; i < 120; i++) statusPool.add(EventStatus.OPEN);
        for (int i = 0; i < 90; i++) statusPool.add(EventStatus.CLOSED);
        Collections.shuffle(statusPool, random);

        Category[] categories = Category.values();

        // 날짜 범위: 2025-01-01 ~ 2026-12-31
        LocalDate rangeStart = LocalDate.of(2025, 1, 1);
        LocalDate rangeEnd = LocalDate.of(2026, 12, 31);
        long rangeDays = ChronoUnit.DAYS.between(rangeStart, rangeEnd);

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            String[] combo = combos.get(i);
            String p1 = combo[0], p2 = combo[1], suffix = combo[2];
            Category category = categories[i % categories.length];

            String title = p1 + p2 + " " + suffix;
            String description = p1 + p2 + " " + category.getName() + " 이벤트입니다. 많은 관심 부탁드립니다.";

            // openDate: 범위 내 랜덤 (endDate 를 위해 90일 여유)
            long offsetDays = (long) (random.nextDouble() * (rangeDays - 90));
            LocalDate openDateBase = rangeStart.plusDays(offsetDays);
            LocalDate endDateBase = openDateBase.plusDays(90);

            // 분단위 받지않기... 그냥 0:0:0 으로 갑시다
            LocalDateTime openDate = openDateBase.atStartOfDay();
            LocalDateTime endDate = endDateBase.atStartOfDay();

            Venue venue = venues.get(random.nextInt(venues.size()));
            EventStatus status;
            LocalDateTime now = LocalDateTime.now();

            if (now.isBefore(openDate)) status = EventStatus.SCHEDULED;
            else if (now.isBefore(endDate)) status = EventStatus.OPEN;
            else status = EventStatus.CLOSED;

            Event event = Event.builder()
                    .venue(venue)
                    .title(title)
                    .description(description)
                    .category(category)
                    .eventStatus(status)
                    .openDate(openDate)
                    .endDate(endDate)
                    .build();

            events.add(event);
        }

        eventRepository.saveAll(events);
        System.out.println("event dummy data 300개 저장 완료!");
    }
}

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
//    }
//}
