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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeFacadeService {

    private final SeatSectionService seatSectionService;
    private final EventSessionService eventSessionService;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketTypeService ticketTypeService;

    public CreateTicketTypeResponse saveTicketType(CreateTicketTypeRequest request){
        List<TicketTypeResponse> responseList = new ArrayList<>();
        EventSession eventSession = eventSessionService.getBySessionId(request.eventSessionId());

        LocalDateTime openDateTime = ticketTypeService.getTicketTypeSaleOpenTime(eventSession);
        LocalDateTime endDateTime = ticketTypeService.getTicketTypeSaleEndTime(eventSession);

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
}
