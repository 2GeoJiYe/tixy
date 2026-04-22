package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.CreateEventRequest;
import com.tixy.api.event.dto.request.GetEventsRequest;
import com.tixy.api.event.dto.request.SessionRequest;
import com.tixy.api.event.dto.request.UpdateEventRequest;
import com.tixy.api.event.dto.response.CreateEventResponse;
import com.tixy.api.event.dto.response.DeleteEventResponse;
import com.tixy.api.event.dto.response.GetEventResponse;
import com.tixy.api.event.entity.Event;
import com.tixy.api.event.enums.EventStatus;
import com.tixy.api.event.repository.EventQueryRepository;
import com.tixy.api.event.repository.EventRepository;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.enums.Category;
import com.tixy.api.venue.service.VenueService;
import com.tixy.core.exception.event.EventErrorCode;
import com.tixy.core.exception.event.EventServiceException;
import com.tixy.core.security.dto.LoginUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventSessionService eventSessionService;
    private final EventRepository eventRepository;
    private final VenueService venueService;
    private final EventQueryRepository eventQueryRepository;
    private final EventRankingService eventRankingService;

    @Transactional
    @CacheEvict(value = "eventSearch", allEntries = true)
    public CreateEventResponse save(CreateEventRequest request) {
        Venue venue = venueService.findVenueById(request.venueId());

        // 가능한 날짜인지 확인하는 로직 추가
        isValidDate(request.openDate(), request.endDate());

        // category 추가
        Category category = Category.from(request.category());

        Event event = Event.builder()
                .venue(venue)
                .title(request.title())
                .description(request.description())
                .category(category)
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

    // param: events Request dto
    // 주어진 조건에 따라 event list 를 paging 하여 return 합니다.
    public List<GetEventResponse> findAll(GetEventsRequest request, Pageable pageable) {
        isValidDate(request.startDate(), request.endDate());
        isValidPrice(request.startPrice(), request.endPrice());

        return eventQueryRepository.findEventsByConditions(request, pageable).getContent();
    }


    @Cacheable(value = "eventSearch",
            key = "#request.hashCode() + '_' + #pageable.pageNumber",
            unless = "#result == null || #result.isEmpty()",
            cacheManager = "localCacheManager")
    public List<GetEventResponse> findAllV2(GetEventsRequest request, Pageable pageable) {
        // 유효성 검증
        isValidDate(request.startDate(), request.endDate());
        isValidPrice(request.startPrice(), request.endPrice());

        List<GetEventResponse> results =
                eventQueryRepository.findEventsByConditions(request, pageable).getContent();

        if (!results.isEmpty()) {
            log.info("Local Cache Miss - DB 조회: {}", results.get(0));
        }

        return results;
    }

    @Cacheable(value = "eventSearchRedis",
            key = "#request.hashCode() + '_' + #pageable.pageNumber",
            unless = "#result == null || #result.isEmpty()",
            cacheManager = "redisCacheManager")
    public List<GetEventResponse> findAllV3 (GetEventsRequest request, Pageable pageable){
        isValidDate(request.startDate(), request.endDate());
        isValidPrice(request.startPrice(), request.endPrice());

        List<GetEventResponse> results =
                eventQueryRepository.findEventsByConditions(request, pageable).getContent();

        if (!results.isEmpty()) {
            log.info("Redis Cache Miss - DB 조회: {}", results.get(0));
        }

        return results;
    }

    // param: event id
    // 해당 event 를 찾아 상세 정보를 조회, return 합니다.
    public GetEventResponse findOne(Long eventId, LoginUserInfoDto userInfo) {
        Event event = findEventById(eventId);
        // redis 호출이 실패해도 event 조회는 할 수 있도록 함
        try{
            eventRankingService.countView(eventId, userInfo.id());
        }catch (Exception e){
            log.warn("[countView] Redis 장애 - eventId: {}, error: {}", eventId, e.getMessage());
        }
        return GetEventResponse.from(event);
    }

    // param: event id, updteEventRequest
    // event 를 조회, update 여부를 확인 후 GetEventResponse 형태로 return 합니다.
    // 해당 이벤트의 예매가 하나라도 시작되었다면 예매 불가능 -> Exception 처리
    // EventStatus 의 변경이 필요하다면 수정
    @Transactional
//    @CacheEvict(value = "event:view", key = "#eventId", cacheManager = "redisCacheManager")
    public GetEventResponse update(Long eventId, UpdateEventRequest request) {

        Event event = findEventById(eventId);

        // SCHEDULED 상태인지 이중검증
        isScheduleStatus(event);

        // 날짜 유효성 검증
        LocalDateTime startDate = request.openDate() == null? event.getOpenDate():request.openDate();
        LocalDateTime endDate = request.endDate() == null? event.getEndDate():request.endDate();
        isValidDate(startDate, endDate);

//         해당 이벤트의 세션 하나라도 예매가 시작되었다면 모든 update 불가능
        if (eventQueryRepository.existsNonPendingTicketTypeByEventId(eventId)){
            throw new EventServiceException(EventErrorCode.RESERVATION_ALREADY_STARTED);
        }

        // venue 값을 업데이트 하고 싶어 하는지 확인
        if (request.venueId() != null){
            Venue venue = venueService.findVenueById(request.venueId());
            event.update(request, venue);
        }else{
            event.update(request, null);
        }

        // end date << now 의 경우 CLOSED
        if (event.getEndDate().isBefore(LocalDateTime.now())) {
            event.updateStatus(EventStatus.CLOSED);
            // start date << now << end date 의 경우 OPEN
        } else if (event.getOpenDate().isBefore(LocalDateTime.now())) {
            event.updateStatus(EventStatus.OPEN);
        }

        eventRankingService.evictViewCache(eventId);

        return GetEventResponse.from(event);
    }


    // event 를 삭제합니다.
    // soft Delete 로 삭제 구문을 요청하면 LocalDateTime deletedAt 과 boolean deleted 이 업데이트 됩니다.
    @Transactional
    public DeleteEventResponse delete(Long eventId) {
        Event event = findEventById(eventId);

        if (eventQueryRepository.existsNonPendingTicketTypeByEventId(eventId)){
            throw new EventServiceException(EventErrorCode.RESERVATION_ALREADY_STARTED);
        }

        event.updateStatus(EventStatus.CLOSED);
        eventRepository.delete(event);
        eventRankingService.evictViewCache(eventId);

        return DeleteEventResponse.from(event);
    }

    public Event findEventById(Long eventId){
        return eventRepository.findById(eventId).orElseThrow(
                ()-> new EventServiceException(EventErrorCode.EVENT_NOT_FOUND)
        );
    }

    private void isScheduleStatus(Event event){
        if (event.getEventStatus() != EventStatus.SCHEDULED) {
            throw new EventServiceException(EventErrorCode.EVENT_NOT_MODIFIABLE);
        }
    }

    // event의 시작 날짜가 종료 날짜보다 앞인지 확인
    private void isValidDate(LocalDateTime startDate, LocalDateTime endDate){
        if (startDate!=null && endDate!=null){
            if (startDate.isAfter(endDate)){
                throw new EventServiceException(EventErrorCode.INVALID_EVENT_DATE);
            }
        }
    }

    private void isValidPrice(Long startPrice, Long endPrice){
        if (startPrice!=null && endPrice!=null){
            if (startPrice > endPrice){
                throw new EventServiceException(EventErrorCode.INVALID_PRICE_FILTER);
            }
        }
    }
}
