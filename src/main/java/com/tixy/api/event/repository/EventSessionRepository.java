package com.tixy.api.event.repository;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventSessionRepository extends JpaRepository<EventSession,Long> {
    List<EventSession> findAllByEventId(Long eventId);

    @Query("""
    SELECT es FROM EventSession es
    WHERE es.status = :status
      AND (:eventSessionId IS NULL OR es.id = :eventSessionId)
""")
    List<EventSession> findAllByStatus(
            @Param("eventSessionId") Long eventSessionId,
            @Param("status") EventSessionStatus status);
}
