package com.tixy.api.ticket.entity;

import com.tixy.api.member.entity.Member;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.ticket.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_session_id", nullable = false, unique = true)
    private SeatSession seatSession;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    private LocalDateTime usedDateTime;

    private LocalDateTime issuedDateTime;
}
