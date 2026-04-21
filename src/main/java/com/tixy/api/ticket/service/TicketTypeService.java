package com.tixy.api.ticket.service;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.service.SeatSectionService;
import com.tixy.api.ticket.dto.request.CreateTicketTypeRequest;
import com.tixy.api.ticket.dto.response.CreateTicketTypeResponse;
import com.tixy.api.ticket.dto.response.TicketTypeResponse;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.enums.TicketTypeStatus;
import com.tixy.api.ticket.repository.TicketTypeRepository;
import com.tixy.core.exception.ticket.TicketTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.tixy.core.exception.ticket.TicketTypeErrorCode.TICKET_TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final SeatSectionService seatSectionService;
    private final EventSessionService eventSessionService;
    private final TicketTypeRepository ticketTypeRepository;

    public CreateTicketTypeResponse saveTicketType(CreateTicketTypeRequest request){
        List<TicketTypeResponse> responseList = new ArrayList<>();
        EventSession eventSession = eventSessionService.getBySessionId(request.eventSessionId());

        LocalDateTime openDateTime = getTicketTypeSaleOpenTime(eventSession);
        LocalDateTime endDateTime = getTicketTypeSaleEndTime(eventSession);

        request.ticketTypeInfos().forEach(items -> {
            SeatSection seatSection = seatSectionService.getBySeatSectionId(items.seatSectionId());

            TicketType ticketType = TicketType.builder()
                    .eventSession(eventSession)
                    .seatSection(seatSection)
                    .price(items.price())
                    .ticketTypeStatus(TicketTypeStatus.PENDING)
                    .saleOpenDateTime(openDateTime)
                    .saleCloseDateTime(endDateTime)
                    .build();
            ticketTypeRepository.save(ticketType);
            TicketTypeResponse response = new TicketTypeResponse(eventSession.getId(),seatSection.getGrade(),items.price());
            responseList.add(response);
        });

        return new CreateTicketTypeResponse(responseList);
    }

    private LocalDateTime getTicketTypeSaleOpenTime(EventSession eventSession) {
        return eventSession.getSessionOpenDate().minusDays(7);
    }

    private LocalDateTime getTicketTypeSaleEndTime(EventSession eventSession) {
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
}
