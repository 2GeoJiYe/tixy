package com.tixy.api.event.repository;

import com.tixy.api.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // EventRepository에 추가
    @Query("SELECT e FROM Event e JOIN FETCH e.venue WHERE e.id = :id")
    Optional<Event> findByIdWithVenue(@Param("id") Long id);
}
