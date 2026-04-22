package com.tixy.core.util;

import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.seat.repository.SeatSessionRepository;
import com.tixy.api.ticket.repository.TicketTypeRepository;
import com.tixy.core.metrics.TixyMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final TixyMetricsService tixyMetricsService;


    // TicketType 상태 전이 - 1분마다
    @Transactional(timeout = 10)
    public void updateTicketTypeStatus(LocalDateTime now) {
        int pendingToOnSaleCount = ticketTypeRepository.updatePendingToOnSale(now);
        int onSaleToEndedCount = ticketTypeRepository.updateOnSaleToSaleEnded(now);

        if (pendingToOnSaleCount > 0) {
            log.info("TicketType PENDING -> ON_SALE: {}건", pendingToOnSaleCount);
        }
        if (onSaleToEndedCount > 0) {
            log.info("TicketType ON_SALE -> SALE_ENDED: {}건", onSaleToEndedCount);
        }
    }

    public void updateEventSessionStatus(LocalDateTime now) {
        int scheduledToOnPerformCount = 0;
        int onPerformToClosedCount = 0;
        int updatedCount;

        do {
            updatedCount = txTemplate.execute(status ->
                    eventSessionRepository.updateToOnPerformBatch(now, 1000)
            );
            scheduledToOnPerformCount += updatedCount;
        } while (updatedCount > 0);

        do {
            updatedCount = txTemplate.execute(status ->
                    eventSessionRepository.updateToClosedBatch(now, 1000)
            );
            onPerformToClosedCount += updatedCount;
        } while (updatedCount > 0);

        if (scheduledToOnPerformCount > 0) {
            log.info("EventSession SCHEDULED -> ON_PERFORM: {}건", scheduledToOnPerformCount);
        }
        if (onPerformToClosedCount > 0) {
            log.info("EventSession ON_PERFORM -> CLOSED: {}건", onPerformToClosedCount);
        }
    }

    @Transactional(timeout = 10)
    public void updateEventStatus(LocalDateTime now) {
        int scheduledToOpenCount = eventRepository.updateToOpen(now);   // SCHEDULED → OPEN
        int openToClosedCount = eventRepository.updateToClosed(now);    // OPEN → CLOSED

        if (scheduledToOpenCount > 0) {
            log.info("Event SCHEDULED -> OPEN: {}건", scheduledToOpenCount);
        }
        if (openToClosedCount > 0) {
            log.info("Event OPEN -> CLOSED: {}건", openToClosedCount);
        }
    }

    @Transactional(timeout = 10)
    public void updateSeatSessionStatus(LocalDateTime now) {
        int releasedCount = seatSessionRepository.releaseExpiredHolds(now); // HELD -> AVAILABLE
        recordSeatSessionReleaseSuccessAfterCommit(now);

        if (releasedCount > 0) {
            log.info("SeatSession HELD -> AVAILABLE: {}건", releasedCount);
        }
    }

    private void recordSeatSessionReleaseSuccessAfterCommit(LocalDateTime now) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            tixyMetricsService.recordSeatSessionReleaseSuccess(now);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                tixyMetricsService.recordSeatSessionReleaseSuccess(now);
            }
        });
    }
}
