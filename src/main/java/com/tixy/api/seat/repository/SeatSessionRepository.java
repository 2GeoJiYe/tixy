package com.tixy.api.seat.repository;

import com.tixy.api.seat.entity.SeatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SeatSessionRepository extends JpaRepository<SeatSession,Long> {

    @Query("SELECT ss FROM SeatSession ss WHERE ss.eventSession.id = :eventSessionId AND ss.seat.id = :seatId")
    Optional<SeatSession> findByEventSessionIdAndSeatId(@Param("eventSessionId") Long eventSessionId, @Param("seatId") Long seatId);
}
