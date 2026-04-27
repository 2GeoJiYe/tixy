package com.tixy.api.seat.repository;

import com.tixy.api.seat.entity.SeatSession;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatSessionRepository extends JpaRepository<SeatSession,Long> {

    @Query("SELECT ss FROM SeatSession ss JOIN FETCH ss.seat WHERE ss.eventSession.id = :eventSessionId AND ss.seat.id IN :seatIds")
    List<SeatSession> findByEventSessionIdAndSeatId(@Param("eventSessionId") Long eventSessionId, @Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "javax.persistence.lock.timeout", value = "0"))
    @Query("SELECT ss FROM SeatSession ss WHERE ss.eventSession.id = :eventSessionId AND ss.seat.id = :seatId")
    Optional<SeatSession> findByEventSessionLock(@Param("eventSessionId") Long eventSessionId, @Param("seatId") Long seatId);

    @Query("SELECT ss FROM SeatSession ss WHERE ss.status = 'AVAILABLE' AND ss.eventSession.id = :eventSessionId")
    List<SeatSession> findAllByEventSessionId(@Param("eventSessionId") Long eventSessionId);

    List<SeatSession> findAllByOrderId(Long orderId);
}
