package com.tixy.core.util;

import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.seat.repository.SeatSessionRepository;
import com.tixy.api.ticket.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;
    private final SeatSessionRepository seatSessionRepository;
    private final TransactionTemplate txTemplate;


    // TicketType 상태 전이 - 1분마다
    @Transactional(timeout = 10)
    public void updateTicketTypeStatus(LocalDateTime now) {
        int ttCnt1 = ticketTypeRepository.updatePendingToOnSale(now);
        int ttCnt2 = ticketTypeRepository.updateOnSaleToSaleEnded(now);

        if (ttCnt1 > 0) log.info("TicketType PENDING → ON_SALE: {}건", ttCnt1);
        if (ttCnt2 > 0) log.info("TicketType ON_SALE → SALE_ENDED: {}건", ttCnt2);
    }

    public void updateEventSessionStatus(LocalDateTime now) {
        int total1 = 0, total2 = 0;
        int updated;

        do {
            updated = txTemplate.execute(status ->
                    eventSessionRepository.updateToOnPerformBatch(now, 1000)
            );
            total1 += updated;
        } while (updated > 0);

        do {
            updated = txTemplate.execute(status ->
                    eventSessionRepository.updateToClosedBatch(now, 1000)
            );
            total2 += updated;
        } while (updated > 0);

        if (total1 > 0) log.info("EventSession SCHEDULED → ON_PERFORM: {}건", total1);
        if (total2 > 0) log.info("EventSession ON_PERFORM → CLOSED: {}건", total2);
    }

    @Transactional(timeout = 10)
    public void updateEventStatus(LocalDateTime now) {
        int cnt1 = eventRepository.updateToOpen(now);   // SCHEDULED → OPEN
        int cnt2 = eventRepository.updateToClosed(now); // OPEN → CLOSED

        if (cnt1 > 0) log.info("event SCHEDULED → OPEN: {}건", cnt1);
        if (cnt2 > 0) log.info("event OPEN → CLOSED: {}건", cnt2);
    }

    @Transactional(timeout = 10)
    public void updateSeatSessionStatus(LocalDateTime now) {
        int ssCnt = seatSessionRepository.releaseExpiredHolds(now);  // HELD -> AVAILABLE
        if (ssCnt > 0) log.info("Seat Session HELD → AVAILABLE: {}건", ssCnt);
    }
}