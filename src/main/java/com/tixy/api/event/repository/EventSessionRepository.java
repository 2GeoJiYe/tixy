package com.tixy.api.event.repository;

import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventSessionRepository extends JpaRepository<EventSession,Long> {
    List<EventSession> findALlByEventId(Long eventId);
    List<EventSession> findAllByStatus(EventSessionStatus status);
    @Modifying
    @Query("UPDATE EventSession s SET s.status = 'ON_PERFORM' "+
            "WHERE s.status = 'SCHEDULED' AND s.sessionOpenDate <= :now")
    int updateToOnPerform(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EventSession s SET s.status = 'CLOSED' "+
            "WHERE s.status = 'ON_PERFORM' AND s.sessionCloseDate <= :now")
    int updateToClosed(@Param("now") LocalDateTime now);
}
