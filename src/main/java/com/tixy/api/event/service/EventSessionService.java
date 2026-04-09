package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.SessionRequest;
import com.tixy.api.event.entity.Event;
import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.event.repository.EventSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EventSessionService {

    private final EventSessionRepository eventSessionRepository;

    public void save(Event event , SessionRequest sessions) {
        EventSession eventSession = EventSession.builder()
                .event(event)
                .session(sessions.session())
                .status(EventSessionStatus.SCHEDULED)
                .sessionOpenDate(sessions.sessionOpenDate())
                .sessionCloseDate(sessions.sessionCloseDate())
                .build();

        eventSessionRepository.save(eventSession);
    }
}
