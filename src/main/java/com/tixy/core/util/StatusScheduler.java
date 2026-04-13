package com.tixy.core.util;

import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.ticket.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusScheduler {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;

    // TicketType 상태 전이 - 1분마다
    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void updateTicketTypeStatus() {
        log.info("Ticket Type Status 전이 스케줄러 시작");
        LocalDateTime now = LocalDateTime.now();

//        ticketTypeRepository.updatePendingToOnSale(now);   // PENDING → ON_SALE
//        ticketTypeRepository.updateOnSaleToSaleEnded(now); // ON_SALE → SALE_ENDED

        int onSaleCount = ticketTypeRepository.updatePendingToOnSale(now);
        int saleEndedCount = ticketTypeRepository.updateOnSaleToSaleEnded(now);

        if (onSaleCount > 0) log.info("TicketType PENDING → ON_SALE: {}건", onSaleCount);
        if (saleEndedCount > 0) log.info("TicketType ON_SALE → SALE_ENDED: {}건", saleEndedCount);
        log.info("Ticket Type Status 전이 스케줄러 종료");
    }

    // EventSession 상태 전이 - 1분마다
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void updateEventSessionStatus() {
        log.info("Event Session Status 전이 스케줄러 시작");
        LocalDateTime now = LocalDateTime.now();

        int cnt1 = eventSessionRepository.updateToOnPerform(now);  // SCHEDULED → ON_PERFORM
        int cnt2 = eventSessionRepository.updateToClosed(now);     // ON_PERFORM → CLOSED

//        int onSaleCount = ticketTypeRepository.updatePendingToOnSale(now);
//        int saleEndedCount = ticketTypeRepository.updateOnSaleToSaleEnded(now);

        if (cnt1 > 0) log.info("eventSession SCHEDULED → ON_PERFROM: {}건", cnt1);
        if (cnt2 > 0) log.info("eventSession ON_PERFROM → CLOSED: {}건", cnt2);

        log.info("Event Session Status 전이 스케줄러 종료");
    }

    // Event 상태 전이 - 매일 자정
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateEventStatus() {
        log.info("Event Status 전이 스케줄러 시작");
        LocalDate today = LocalDate.now();

        eventRepository.updateToOpen(today);   // SCHEDULED → OPEN
        eventRepository.updateToClosed(today); // OPEN → CLOSED
        log.info("Event Status 전이 스케줄러 종료");
    }
}