package com.tixy.api.seat.entity;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.seat.enums.SessionSeatStatus;
import com.tixy.core.exception.seat.SeatErrorCode;
import com.tixy.core.exception.seat.SeatException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Table(name = "seat_sessions", indexes = {
        @Index(name = "idx_seat_sessions_event_session_seat", columnList = "event_session_id, seat_id", unique = true)
})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
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

    private Long userId; // 매핑까지는 필요없고 그냥 유저가 있다는 정보만 있으면 될거같음.

    private LocalDateTime expireAt;

    public void setHeld(Long userId) {
        if(this.status != SessionSeatStatus.AVAILABLE){
            throw new SeatException(SeatErrorCode.INVALID_SEAT_SESSION_STATUS);
        }
        LocalDateTime now = LocalDateTime.now();
        this.status = SessionSeatStatus.HELD;
        this.userId = userId;
        this.expireAt = now.plusMinutes(5);
    }

    public void reserved(){
        if(this.status != SessionSeatStatus.HELD){
            throw new SeatException(SeatErrorCode.INVALID_SEAT_SESSION_STATUS);
        }
        this.status = SessionSeatStatus.RESERVED;
    }
}
