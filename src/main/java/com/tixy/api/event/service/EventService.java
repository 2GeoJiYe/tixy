package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.CreateEventRequest;
import com.tixy.api.event.dto.request.SessionRequest;
import com.tixy.api.event.dto.response.CreateEventResponse;
import com.tixy.api.event.entity.Event;
import com.tixy.api.event.enums.EventStatus;
import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventSessionService eventSessionService;
    private final EventRepository eventRepository;
    private final VenueService venueService;

    @Transactional
    public CreateEventResponse save(CreateEventRequest request) {
        Venue venue = venueService.findVenueById(request.venueId());
        Event event = Event.builder()
                .venue(venue)
                .title(request.title())
                .description(request.description())
                .eventStatus(EventStatus.SCHEDULED)
                .openDate(request.openDate())
                .endDate(request.endDate())
                .build();

        eventRepository.save(event);

        // 여기서는 그냥 for문 돌리고 벌크인서트 안한건 좌석에 비해서 회차 정보는 이벤트당 그렇게 까지 많이 데이터가 없을거같아서
        // 성능에 큰 이슈가 없을거라고 생각했습니다. 뮤지컬 같은거 하루에 두번하고 일주일에 3번 2달 하면 48~50 회차 정도 나올텐데 그정도는...괜찮지 않나..(저얼대 귀찮아서가 아님ㅇㅇ)
        for (SessionRequest sessionRequest : request.sessions()) {
            eventSessionService.save(event, sessionRequest);
        }

        return new CreateEventResponse(request.title(), request.description());
    }
}
