package com.tixy.api.ticket.service;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.repository.TicketTypeRepository;
import com.tixy.core.exception.ticket.TicketTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.tixy.core.exception.ticket.TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    public LocalDateTime getTicketTypeSaleOpenTime(EventSession eventSession) {
        return eventSession.getSessionOpenDate().minusDays(7);
    }

    public LocalDateTime getTicketTypeSaleEndTime(EventSession eventSession) {
        return eventSession.getSessionOpenDate().minusHours(1);
    }

    public TicketType getTicketTypeByEventSessionId(Long eventSessionId, Long seatSectionId) {
        return ticketTypeRepository.findByEventSessionAndSeatSectionId(eventSessionId,seatSectionId).orElseThrow(
                () -> new TicketTypeException(TICKET_TYPE_NOT_FOUND)
        );
    }

    public TicketType getTicketTypeById(Long ticketTypeId) {
        return ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new TicketTypeException(TICKET_TYPE_NOT_FOUND)
        );
    }

    public void checkTypeSaleOpen(Long eventSessionId, Long seatSectionId) {
        TicketType ticketType = getTicketTypeByEventSessionId(eventSessionId, seatSectionId);

        ticketType.checkOpenSale();
    }
}
