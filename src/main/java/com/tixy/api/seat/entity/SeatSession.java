package com.tixy.api.seat.entity;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.seat.enums.SessionSeatStatus;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "seat_sessions")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class SeatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id" , nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_session_id" , nullable = false)
    private EventSession eventSession;

    @Enumerated(EnumType.STRING)
    private SessionSeatStatus status;
}
