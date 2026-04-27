package com.tixy.api.ticket.entity;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.ticket.enums.TicketTypeStatus;
import com.tixy.core.exception.event.EventErrorCode;
import com.tixy.core.exception.event.EventServiceException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_types")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_session_id" , nullable = false)
    private EventSession eventSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_section_id" , nullable = false)
    private SeatSection seatSection;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    private TicketTypeStatus ticketTypeStatus;

    @Column(nullable = false)
    private LocalDateTime saleOpenDateTime;

    @Column(nullable = false)
    private LocalDateTime saleCloseDateTime;

    public void checkOpenSale(){
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(saleOpenDateTime) || now.isAfter(saleCloseDateTime)){
            throw new EventServiceException(EventErrorCode.EVENT_SESSION_NOT_SALE);
        }
    }
}
