package com.tixy.api.event.repository;

import com.tixy.api.event.entity.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventSessionRepository extends JpaRepository<EventSession,Long> {
    List<EventSession> findALlByEventId(Long eventId);
}
