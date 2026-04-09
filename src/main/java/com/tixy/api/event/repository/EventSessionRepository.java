package com.tixy.api.event.repository;

import com.tixy.api.event.entity.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventSessionRepository extends JpaRepository<EventSession,Long> {
}
