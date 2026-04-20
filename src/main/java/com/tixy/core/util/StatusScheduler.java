package com.tixy.core.util;

import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.seat.repository.SeatSessionRepository;
import com.tixy.api.ticket.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
public class StatusScheduler {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;
    private final SeatSessionRepository seatSessionRepository;

    private LocalDateTime lastSeatSessionUpdate = LocalDateTime.MIN;

    @Scheduled(fixedDelay = 60000)
    @Transactional(timeout = 10)
    public void updateAllStatuses() {
        // 상위 → 하위 순서로 실행 (Session → TicketType → Seat)
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() == 0 && now.getMinute() == 0) {
            return; // 0시 0분 (event scheduler 도는 시간) 에는 skip
        }

        // event session
        System.out.println("event session scheduler started");

        int esCnt1 = eventSessionRepository.updateToOnPerform(now);  // SCHEDULED → ON_PERFORM
        int esCnt2 = eventSessionRepository.updateToClosed(now);     // ON_PERFORM → CLOSED

        if (esCnt1 > 0) log.info("eventSession SCHEDULED → ON_PERFROM: {}건", esCnt1);
        if (esCnt2 > 0) log.info("eventSession ON_PERFROM → CLOSED: {}건", esCnt2);

        // ticket type
        System.out.println("ticket type scheduler started");

        int ttCnt1 = ticketTypeRepository.updatePendingToOnSale(now);
        int ttCnt2 = ticketTypeRepository.updateOnSaleToSaleEnded(now);

        if (ttCnt1 > 0) log.info("TicketType PENDING → ON_SALE: {}건", ttCnt1);
        if (ttCnt2 > 0) log.info("TicketType ON_SALE → SALE_ENDED: {}건", ttCnt2);

        // 얘는 5분에 한 번씩 실행되게 설정
        if (now.isAfter(lastSeatSessionUpdate.plusMinutes(5))){
            System.out.println("seat session scheduler started");

            int ssCnt = seatSessionRepository.releaseExpiredHolds(now);  // HELD -> AVAILABLE
            if (ssCnt > 0) log.info("Seat Session HELD → AVAILABLE: {}건", ssCnt);
            lastSeatSessionUpdate = now;
        }
    }

    // Event 상태 전이 - 매일 자정
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateEventStatus() {
        LocalDate today = LocalDate.now();

        int cnt1 = eventRepository.updateToOpen(today);   // SCHEDULED → OPEN
        int cnt2 = eventRepository.updateToClosed(today); // OPEN → CLOSED
        if (cnt1 > 0) log.info("event SCHEDULED → OPEN: {}건", cnt1);
        if (cnt2 > 0) log.info("event OPEN → CLOSED: {}건", cnt2);
    }
}