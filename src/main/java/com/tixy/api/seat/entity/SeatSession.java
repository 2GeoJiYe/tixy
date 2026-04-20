package com.tixy.api.seat.entity;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.order.entity.Order;
import com.tixy.api.seat.enums.SessionSeatStatus;
import com.tixy.core.exception.seat.SeatErrorCode;
import com.tixy.core.exception.seat.SeatException;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Slf4j
@Table(name = "seat_sessions")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public void setOrder(Order order) {
        if(this.status != SessionSeatStatus.HELD){
            throw new SeatException(SeatErrorCode.INVALID_SEAT_SESSION_STATUS);
        }
        if(this.userId == null){
            throw new SeatException(SeatErrorCode.SEAT_SESSION_USER_NOT_FOUND);
        }
        log.info(order.getOrderNo());
        this.order = order;
    }

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

    public void unHeld(){
        if(this.status != SessionSeatStatus.HELD){
            throw new SeatException(SeatErrorCode.INVALID_SEAT_SESSION_STATUS);
        }
        this.status = SessionSeatStatus.AVAILABLE;
        this.expireAt = null;
        this.userId = null;
    }

    public void checkExpired(){
        LocalDateTime now = LocalDateTime.now();
        if(this.expireAt != null && now.isAfter(this.expireAt)){
            throw new SeatException(SeatErrorCode.SEAT_SESSION_EXPIRE);
        }
    }
}
