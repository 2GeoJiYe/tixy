package com.tixy.api.event.entity;

import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.core.entity.BaseEntity;
import com.tixy.core.exception.event.EventErrorCode;
import com.tixy.core.exception.event.EventServiceException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Table(name = "event_sessions")
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id" , nullable = false)
    private Event event;

    @Column(nullable = false)
    private String session; // -> 1회차, 2회차 같은 회차정보임

    @Column(nullable = false)
    private Long sessionSeatCount;

    @Enumerated(EnumType.STRING)
    private EventSessionStatus status;

    @Column(updatable = false)
    private LocalDateTime sessionOpenDate;

    @Column(updatable = false)
    private LocalDateTime sessionCloseDate;

    public void checkOpenSale(){
        if(this.status == EventSessionStatus.CLOSED){
            throw new EventServiceException(EventErrorCode.EVENT_SESSION_CLOSED);
        }

        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(sessionOpenDate) || now.isAfter(sessionCloseDate)){
            throw new EventServiceException(EventErrorCode.EVENT_SESSION_NOT_SALE);
        }
    }

}
