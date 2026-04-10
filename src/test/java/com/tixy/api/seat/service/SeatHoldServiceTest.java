package com.tixy.api.seat.service;

import com.tixy.api.event.entity.Event;
import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.seat.enums.Grade;
import com.tixy.api.seat.enums.SeatStatus;
import com.tixy.api.seat.enums.SessionSeatStatus;
import com.tixy.api.seat.repository.SeatRepository;
import com.tixy.api.seat.repository.SeatSectionRepository;
import com.tixy.api.seat.repository.SeatSessionRepository;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class SeatHoldServiceTest {
    @Autowired
    private SeatSectionRepository seatSectionRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatHoldService seatHoldService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private EventSessionRepository eventSessionRepository;

    @Autowired
    private SeatSessionRepository seatSessionRepository;

    private Long eventSessionId;
    private Long seatId;

    @BeforeEach
    void setUp() {
        Venue venue = Venue.builder()
                .name("테스트 공연장")
                .build();
        venueRepository.save(venue);

        SeatSection seatSection = SeatSection.builder()
                .venue(venue)
                .sectionName("1")
                .grade(Grade.AGRADE)
                .build();

        seatSectionRepository.save(seatSection);

        Event event = Event.builder()
                .venue(venue)
                .title("테스트 공연")
                .description("테스트")
                .build();
        eventRepository.save(event);

        EventSession eventSession = EventSession.builder()
                .event(event)
                .session("1")
                .status(EventSessionStatus.SCHEDULED)
                .sessionOpenDate(LocalDateTime.now().minusHours(1))
                .sessionCloseDate(LocalDateTime.now().plusHours(1))
                .build();
        eventSessionId = eventSessionRepository.save(eventSession).getId();

        Seat seat = Seat.builder()
                .seatSection(seatSection)
                .seatStatus(SeatStatus.ACTIVE)
                .build();
        seatRepository.save(seat);

        SeatSession seatSession = SeatSession.builder()
                .seat(seat)
                .eventSession(eventSession)
                .status(SessionSeatStatus.AVAILABLE)
                .build();
        seatId = seatSessionRepository.save(seatSession).getId();
    }


    @Test
    void 동시에_여러사람이_하나의_자리예약_테스트() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1L;
            executor.submit(() -> {
                try {
                    barrier.await(); // 모든 스레드 준비될 때까지 대기 후 동시 출발
                    seatHoldService.seatHold(eventSessionId, seatId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }

}