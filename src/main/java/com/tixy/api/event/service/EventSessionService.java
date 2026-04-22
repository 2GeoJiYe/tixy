package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.SessionRequest;
import com.tixy.api.event.dto.response.GetEventSessionsResponse;
import com.tixy.api.event.dto.response.GetOneEventSessionResponse;
import com.tixy.api.event.entity.Event;
import com.tixy.api.event.entity.EventSession;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.event.repository.EventQueryRepository;
import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.event.repository.EventSessionRepository;
import com.tixy.api.ticket.dto.response.TicketSaleDateResponse;
import com.tixy.core.exception.event.EventErrorCode;
import com.tixy.core.exception.event.EventServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class EventSessionService {

    private final EventSessionRepository eventSessionRepository;
    private final EventQueryRepository eventQueryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public void save(Event event , SessionRequest sessions) {
        // seat Count 관련 내용 추가
        // 따로 request 에서 입력받지 않으면 venue 의 seat 개수를 넣기
        Long seatCount;
        if (sessions.sessionSeatCount() == null){
            seatCount = event.getVenue().getTotalSeatCount();
        }else{
            seatCount = sessions.sessionSeatCount();
        }

        EventSession eventSession = EventSession.builder()
                .event(event)
                .session(sessions.session())
                .sessionSeatCount(sessions.sessionSeatCount())
                .status(EventSessionStatus.SCHEDULED)
                .sessionSeatCount(seatCount)
                .sessionOpenDate(sessions.sessionOpenDate())
                .sessionCloseDate(sessions.sessionCloseDate())
                .build();

        eventSessionRepository.save(eventSession);

    }

    public Page<GetEventSessionsResponse> findAll(Long eventId, Pageable pageable) {
        return eventQueryRepository.findSessionsByEventId(eventId, pageable);
    }

    public GetOneEventSessionResponse findOne(Long eventId, Long scheduleId) {
        // 순환참조 뜨길래 repository 에서 바로 가져오는걸로 수정했습니다
        Event event = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventServiceException(EventErrorCode.EVENT_NOT_FOUND)
        );


        EventSession session = getBySessionId(scheduleId);
        // session 이 해당 event 에 속하는지 검증
        if (!session.getEvent().getId().equals(eventId)) {
            throw new EventServiceException(EventErrorCode.EVENT_SESSION_NOT_FOUND);
        }

        TicketSaleDateResponse dateResponse = eventQueryRepository.findSaleDateBySessionId(scheduleId);

        return new GetOneEventSessionResponse(
                event.getTitle(),
                session.getSessionSeatCount(),
                session.getStatus().getStatus(),
                session.getSessionOpenDate(),
                session.getSessionCloseDate(),
                dateResponse.saleOpenDateTime(),
                dateResponse.saleCloseDateTime(),
                eventQueryRepository.findTicketPriceListBySessionId(eventId, scheduleId)
        );
    }


    public List<EventSession> getAllByEventId(Long eventId) {
        return eventSessionRepository.findALlByEventId(eventId);
    }

    public void checkSessionSaleOpen(Long sessionId) {
        EventSession eventSession = getBySessionId(sessionId);
        eventSession.checkOpenSale();
    }

    public EventSession getBySessionId(Long sessionId) {
        return eventSessionRepository.findById(sessionId).orElseThrow(
                () -> new EventServiceException(EventErrorCode.EVENT_NOT_FOUND)
        );
    }

    public List<Long> getOnPerformEventSessionIds(){
        List<Long> eventSessionIds = new ArrayList<>();
        List<EventSession> sessionList = eventSessionRepository.findAllByStatus(EventSessionStatus.ON_PERFORM);
        for (EventSession eventSession : sessionList) {
            eventSessionIds.add(eventSession.getId());
        }
        return eventSessionIds;
    }


}
