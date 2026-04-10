package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.SessionRequest;
import com.tixy.api.event.entity.Event;
import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.core.exception.event.EventErrorCode;
import com.tixy.core.exception.event.EventServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


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

    public EventSession getBySessionId(Long sessionId) {
        return eventSessionRepository.findById(sessionId).orElseThrow(
                () -> new EventServiceException(EventErrorCode.EVENT_NOT_FOUND)
        );
    }

    public List<EventSession> getAllByEventId(Long eventId) {
        return eventSessionRepository.findALlByEventId(eventId);
    }
}
