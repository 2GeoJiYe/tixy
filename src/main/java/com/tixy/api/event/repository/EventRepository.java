package com.tixy.api.event.repository;

import com.tixy.api.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // EventRepository에 추가
    @Query("SELECT e FROM Event e JOIN FETCH e.venue WHERE e.id = :id")
    Optional<Event> findByIdWithVenue(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Event e SET e.eventStatus = 'OPEN' " +
            "WHERE e.eventStatus = 'SCHEDULED' AND e.openDate <= :now")
    int updateToOpen(@Param("now") LocalDateTime today);

    @Modifying
    @Query("UPDATE Event e SET e.eventStatus = 'CLOSED' " +
            "WHERE e.eventStatus = 'OPEN' AND e.endDate <= :now")
    int updateToClosed(@Param("now") LocalDateTime today);
}
