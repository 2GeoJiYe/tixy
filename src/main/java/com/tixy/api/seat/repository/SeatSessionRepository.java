package com.tixy.api.seat.repository;

import com.tixy.api.seat.entity.SeatSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatSessionRepository extends JpaRepository<SeatSession,Long> {

    @Query("SELECT ss FROM SeatSession ss JOIN FETCH ss.seat WHERE ss.eventSession.id = :eventSessionId AND ss.seat.id IN :seatIds")
    List<SeatSession> findByEventSessionIdAndSeatId(@Param("eventSessionId") Long eventSessionId, @Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM SeatSession ss WHERE ss.eventSession.id = :eventSessionId AND ss.seat.id = :seatId")
    Optional<SeatSession> findByEventSessionLock(@Param("eventSessionId") Long eventSessionId, @Param("seatId") Long seatId);

    @Modifying
    @Query("UPDATE SeatSession s SET s.status = 'AVAILABLE', s.userId = null, s.expireAt = null " +
            "WHERE s.status = 'HELD' AND s.expireAt <= :now")
    int releaseExpiredHolds(@Param("now") LocalDateTime now);

    List<SeatSession> findAllByOrderId(Long orderId);
}
