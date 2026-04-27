package com.tixy.api.ticket.repository;

import com.tixy.api.ticket.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    @Query("""
    SELECT tt FROM TicketType tt
    JOIN FETCH tt.eventSession es
    JOIN FETCH es.event
    WHERE tt.eventSession.id = :eventSessionId
    AND tt.seatSection.id = :seatSectionId
    """)
    Optional<TicketType> findByEventSessionAndSeatSectionId(@Param("eventSessionId") Long eventSessionId, @Param("seatSectionId") Long seatSectionId);
}
