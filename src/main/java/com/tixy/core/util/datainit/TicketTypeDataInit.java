package com.tixy.core.util.datainit;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.repository.SeatSectionRepository;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.enums.TicketTypeStatus;
import com.tixy.api.ticket.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TicketTypeDataInit {
    private final TicketTypeRepository ticketTypeRepository;
    private final EventSessionRepository eventSessionRepository;
    private final SeatSectionRepository seatSectionRepository;

    @Transactional
    public void initTicketTypes() {
        if (ticketTypeRepository.count() > 0) return;

        List<EventSession> sessions = eventSessionRepository.findAll();
        Random random = new Random(42);
        LocalDateTime now = LocalDateTime.now();

        // N+1 방지 - seatSection 미리 로드
        Map<Long, List<SeatSection>> venueSeatSectionMap = seatSectionRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(ss -> ss.getVenue().getId()));

        // seatSection별 가격 고정
        Map<Long, Long> seatSectionPriceMap = new HashMap<>();

        List<TicketType> ticketTypes = new ArrayList<>();

        for (EventSession session : sessions) {
            Long venueId = session.getEvent().getVenue().getId();
            List<SeatSection> seatSections = venueSeatSectionMap.getOrDefault(venueId, List.of());

            for (SeatSection seatSection : seatSections) {
                long price = seatSectionPriceMap.computeIfAbsent(seatSection.getId(), k ->
                        (long) switch (seatSection.getGrade()) {
                            case NORMAL -> 20000 + random.nextInt(5) * 5000;
                            case BGRADE -> 40000 + random.nextInt(5) * 5000;
                            case AGRADE -> 60000 + random.nextInt(5) * 5000;
                            case ROYAL  -> 80000 + random.nextInt(5) * 5000;
                            case VIP    -> 100000 + random.nextInt(5) * 10000;
                        }
                );

                LocalDateTime sessionOpen = session.getSessionOpenDate();

                // 판매 시작: sessionOpen 기준 최대 2달 전, event openDate 앞뒤 상관없음
                long maxSaleDays = Math.min(60, ChronoUnit.DAYS.between(
                        session.getEvent().getOpenDate(), sessionOpen) + 60);
                long saleDaysOffset = 1 + (long) (random.nextDouble() * (maxSaleDays - 1));
                LocalDateTime saleOpen = sessionOpen.minusDays(saleDaysOffset).withMinute(0);

                // 판매 종료: 세션 시작 10분 전
                LocalDateTime saleClose = sessionOpen.minusMinutes(10);

                TicketTypeStatus ticketTypeStatus;
                if (now.isBefore(saleOpen)) {
                    ticketTypeStatus = TicketTypeStatus.PENDING;
                } else if (now.isBefore(saleClose)) {
                    ticketTypeStatus = TicketTypeStatus.ON_SALE;
                } else {
                    ticketTypeStatus = TicketTypeStatus.SALE_ENDED;
                }

                ticketTypes.add(TicketType.builder()
                        .eventSession(session)
                        .seatSection(seatSection)
                        .price(price)
                        .ticketTypeStatus(ticketTypeStatus)
                        .saleOpenDateTime(saleOpen)
                        .saleCloseDateTime(saleClose)
                        .build());
            }
        }

        ticketTypeRepository.saveAll(ticketTypes);
        System.out.println("ticketType dummy data " + ticketTypes.size() + "개 저장 완료!");
    }

}
