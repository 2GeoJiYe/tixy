package com.tixy.api.seat.entity;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.seat.enums.SessionSeatStatus;
import com.tixy.core.exception.seat.SeatErrorCode;
import com.tixy.core.exception.seat.SeatException;
import jakarta.persistence.*;
import lombok.*;


@Table(name = "seat_sessions", indexes = {
        @Index(name = "idx_seat_sessions_event_session_seat", columnList = "event_session_id, seat_id", unique = true)
})
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

    public void setHeld(){
        if(this.status != SessionSeatStatus.AVAILABLE){
            throw new SeatException(SeatErrorCode.INVALID_SEAT_SESSION_STATUS);
        }
        this.status = SessionSeatStatus.HELD;
    }
}
