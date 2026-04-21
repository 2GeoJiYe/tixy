package com.tixy.api.ticket.service;


import com.tixy.api.member.entity.Member;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.ticket.dto.response.CreateTicketResponse;
import com.tixy.api.ticket.entity.Ticket;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.api.ticket.enums.TicketStatus;
import com.tixy.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public List<CreateTicketResponse> createTickets(List<SeatSession> seatSessions,TicketType ticketType, Member member ) {
        List<CreateTicketResponse> createTicketResponses = new ArrayList<>();
        seatSessions.forEach(seatSession -> {
            Ticket ticket = createTicket(ticketType, seatSession, member);
            CreateTicketResponse response = new CreateTicketResponse(ticket.getId(), ticket.getSeatSession().getSeat().getRowLabel());
            createTicketResponses.add(response);
        });

        return createTicketResponses;
    }

    private Ticket createTicket(TicketType ticketType, SeatSession seatSession, Member member) {
        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = Ticket.builder()
                .ticketType(ticketType)
                .seatSession(seatSession)
                .member(member)
                .ticketStatus(TicketStatus.PENDING)
                .issuedDateTime(now)
                .build();

        return ticketRepository.save(ticket);
    }
}
