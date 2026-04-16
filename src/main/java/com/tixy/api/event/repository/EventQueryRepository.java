package com.tixy.api.event.repository;

import com.tixy.api.event.dto.request.GetEventsRequest;
import com.tixy.api.event.dto.response.GetEventResponse;
import com.tixy.api.event.dto.response.GetEventSessionsResponse;
import com.tixy.api.event.dto.response.GetRankedEventResponse;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.event.enums.EventStatus;
import com.tixy.api.ticket.dto.response.TicketSaleDateResponse;
import com.tixy.api.ticket.enums.TicketTypeStatus;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tixy.jooq.tixy.Tables.SEAT_SECTIONS;
import static com.tixy.jooq.tixy.Tables.VENUES;
import static com.tixy.jooq.tixy.tables.Events.EVENTS;
import static com.tixy.jooq.tixy.tables.EventSessions.EVENT_SESSIONS;
import static com.tixy.jooq.tixy.tables.TicketTypes.TICKET_TYPES;


@Repository
@RequiredArgsConstructor
public class EventQueryRepository {

    private static final long TOP_N = 10;
    private final DSLContext dsl;

    //하나라도 PENDING 상태가 아닌 ticket type 이 있다면 수정,삭제 불가능
    //true -> EventStatus 는 모두 SCHEDULED (OPEN 된 이상 무조건 예매가 시작되었음)
    //false -> 모든 EventStatus 가능
    public boolean existsNonPendingTicketTypeByEventId(Long eventId){
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(EVENTS)
                        .join(EVENT_SESSIONS).on(EVENTS.ID.eq(EVENT_SESSIONS.EVENT_ID))
                        .join(TICKET_TYPES).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                        .where(EVENTS.ID.eq(eventId))
                        .and(TICKET_TYPES.TICKET_TYPE_STATUS.ne(String.valueOf(TicketTypeStatus.PENDING))));
    }

    // 예매가능 여부, 지역(여러지역이 선택될 수 있음), event 의 시작날짜와 종료날짜, 키워드 contains, 가격 최소값, 가격 최댓값
    public Page<GetEventResponse> findEventsByConditions(GetEventsRequest request, Pageable pageable) {
        var conditions = DSL.noCondition(); // 처음에는 condition 을 no Condition 으로 해두고 추가하기

        // 지역 필터 (여러 지역이 값으로 들어올 수도 있으니까 List 로 받아서 in 조건 주기...)
        if (request.area() != null && !request.area().isEmpty()) {
            conditions = conditions.and(VENUES.LOCATION.in(request.area()));
        }

        // 카테고리 필터 (여러 카테고리값 가능)
        if (request.category() != null && !request.category().isEmpty()) {
            conditions = conditions.and(EVENTS.CATEGORY.in(request.category()));
        }

        // 날짜 필터
        // open < end 인건 service 에서 확인하고 넘기기
        if (request.startDate() != null) {
            conditions = conditions.and(EVENTS.OPEN_DATE.ge(request.startDate()));
        }
        if (request.endDate() != null) {
            conditions = conditions.and(EVENTS.END_DATE.le(request.endDate()));
        }

        // 가격 필터
        // start < end 인것도 service 에서 확인하기
        if (request.startPrice() != null) {
            conditions = conditions.and(TICKET_TYPES.PRICE.ge(request.startPrice()));
        }
        if (request.endPrice() != null) {
            conditions = conditions.and(TICKET_TYPES.PRICE.le(request.endPrice()));
        }

        // 키워드 필터 (title OR description)
        if (request.keyword() != null && !request.keyword().isBlank()) {
            conditions = conditions.and(
                    EVENTS.TITLE.containsIgnoreCase(request.keyword())
                            .or(EVENTS.DESCRIPTION.containsIgnoreCase(request.keyword()))
            );
        }

        if (Boolean.TRUE.equals(request.reservePossible())) {
            conditions = conditions.and(
                    TICKET_TYPES.TICKET_TYPE_STATUS.in("PENDING", "ON_SALE")
            );
            conditions = conditions.and(
                    EVENT_SESSIONS.STATUS.ne(String.valueOf(EventSessionStatus.CLOSED))
            );
            conditions = conditions.and(
                    EVENTS.EVENT_STATUS.ne(String.valueOf(EventStatus.CLOSED))
            );
        }

        var query = dsl.selectDistinct(
                        EVENTS.ID,
                        EVENTS.TITLE,
                        EVENTS.DESCRIPTION,
                        EVENTS.EVENT_STATUS,
                        EVENTS.OPEN_DATE,
                        EVENTS.END_DATE,
                        VENUES.LOCATION,
                        VENUES.NAME)
                .from(EVENTS)
//                .leftJoin(EVENT_SESSIONS).on(EVENTS.ID.eq(EVENT_SESSIONS.EVENT_ID))
//                .leftJoin(TICKET_TYPES).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
//                .leftJoin(VENUES).on(VENUES.ID.eq(EVENTS.VENUE_ID))
                .join(EVENT_SESSIONS).on(EVENTS.ID.eq(EVENT_SESSIONS.EVENT_ID))
                .join(TICKET_TYPES).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                .join(VENUES).on(VENUES.ID.eq(EVENTS.VENUE_ID))

                .where(conditions);

        // 전체 count (페이징용)
        int total = dsl.fetchCount(query);

        List<GetEventResponse> results = query
                .orderBy(EVENTS.OPEN_DATE.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(record -> new GetEventResponse(
                        record.get(EVENTS.ID),
                        record.get(EVENTS.TITLE),
                        record.get(EVENTS.DESCRIPTION),
                        record.get(VENUES.LOCATION),
                        record.get(VENUES.NAME),
                        record.get(EVENTS.EVENT_STATUS),
                        record.get(EVENTS.OPEN_DATE),
                        record.get(EVENTS.END_DATE)
                ));

        return new PageImpl<>(results, pageable, total);

    }

    // 특정 이벤트 세션의 티켓 가격 리스트를 반환
    public Map<String, Long> findTicketPriceListBySessionId(Long eventId, Long sessionId) {
        return dsl.select(SEAT_SECTIONS.GRADE, TICKET_TYPES.PRICE)
                .from(EVENT_SESSIONS)
                .join(TICKET_TYPES).on(EVENT_SESSIONS.ID.eq(TICKET_TYPES.EVENT_SESSION_ID))
                .join(SEAT_SECTIONS).on(TICKET_TYPES.SEAT_SECTION_ID.eq(SEAT_SECTIONS.ID))
                .where(EVENT_SESSIONS.ID.eq(sessionId))
                .fetch()
                .intoMap(
                        record -> record.get(SEAT_SECTIONS.GRADE),
                        record -> record.get(TICKET_TYPES.PRICE)
                );
    }

    public TicketSaleDateResponse findSaleDateBySessionId(Long sessionId) {
        return dsl.select(TICKET_TYPES.SALE_OPEN_DATE_TIME, TICKET_TYPES.SALE_CLOSE_DATE_TIME)
                .from(TICKET_TYPES)
                .where(TICKET_TYPES.EVENT_SESSION_ID.eq(sessionId))
                .limit(1)
                .fetchOne(record -> new TicketSaleDateResponse(
                        record.get(TICKET_TYPES.SALE_OPEN_DATE_TIME),
                        record.get(TICKET_TYPES.SALE_CLOSE_DATE_TIME)
                ));
    }

    // 특정 event 에 대한 세션들의 정보
    public Page<GetEventSessionsResponse> findSessionsByEventId(Long eventId, Pageable pageable) {

        var query = dsl.select(
                        EVENT_SESSIONS.ID,
                        EVENTS.TITLE,
                        EVENT_SESSIONS.SESSION_SEAT_COUNT,
                        EVENT_SESSIONS.STATUS,
                        EVENT_SESSIONS.SESSION_OPEN_DATE,
                        EVENT_SESSIONS.SESSION_CLOSE_DATE,
                        DSL.min(TICKET_TYPES.PRICE).as("min_price"),
                        DSL.max(TICKET_TYPES.PRICE).as("max_price")
                )
                .from(EVENT_SESSIONS)
                .join(EVENTS).on(EVENT_SESSIONS.EVENT_ID.eq(EVENTS.ID))
                .join(TICKET_TYPES).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                .where(EVENT_SESSIONS.EVENT_ID.eq(eventId))
                .groupBy(EVENT_SESSIONS.ID);

        int total = dsl.fetchCount(query);

        List<GetEventSessionsResponse> results = query
                .orderBy(EVENT_SESSIONS.SESSION_OPEN_DATE.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(record -> new GetEventSessionsResponse(
                        record.get(EVENT_SESSIONS.ID),
                        record.get(EVENTS.TITLE),
                        record.get(EVENT_SESSIONS.SESSION_SEAT_COUNT),
                        record.get(EVENT_SESSIONS.STATUS),
                        record.get(EVENT_SESSIONS.SESSION_OPEN_DATE),
                        record.get(EVENT_SESSIONS.SESSION_CLOSE_DATE),
                        record.get(DSL.min(TICKET_TYPES.PRICE).as("min_price"), Long.class),
                        record.get(DSL.max(TICKET_TYPES.PRICE).as("max_price"), Long.class)
                ));

        return new PageImpl<>(results, pageable, total);
    }


//    JOOQ - 상세 조회 + 카테고리 필터링 + Top N 컷
//    category = null 이면 전체
    public List<GetRankedEventResponse> fetchScheduleDetails(
            List<Long> scheduleIds,
            Map<Long, Double> scoreMap,
            String category
    ) {
        var conditions = DSL.noCondition();

        conditions = conditions.and(EVENTS.ID.in(scheduleIds));
        conditions = conditions.and(EVENTS.DELETED_AT.isNull());

        if (category != null) {
            conditions = conditions.and(EVENTS.CATEGORY.eq(category));
        }

        return dsl.select(
                        EVENTS.ID,
                        EVENTS.TITLE,
                        EVENTS.DESCRIPTION,
                        EVENTS.EVENT_STATUS,
                        EVENTS.OPEN_DATE,
                        EVENTS.END_DATE,
                        EVENTS.CATEGORY,
                        VENUES.LOCATION,
                        VENUES.NAME)
                .from(EVENTS)
                .join(VENUES).on(VENUES.ID.eq(EVENTS.VENUE_ID))
                .where(conditions)
                .fetch(record -> new GetRankedEventResponse(
                        record.get(EVENTS.CATEGORY),
                        new GetEventResponse(
                                record.get(EVENTS.ID),
                                record.get(EVENTS.TITLE),
                                record.get(EVENTS.DESCRIPTION),
                                record.get(VENUES.LOCATION),
                                record.get(VENUES.NAME),
                                record.get(EVENTS.EVENT_STATUS),
                                record.get(EVENTS.OPEN_DATE),
                                record.get(EVENTS.END_DATE)
                        ),
                        scoreMap.getOrDefault(record.get(EVENTS.ID), 0.0).longValue()
                ))
                .stream()
                .sorted(Comparator.comparingLong(GetRankedEventResponse::viewScore).reversed())
                .limit(TOP_N)
                .collect(Collectors.toList());
    }

    // redis 가 비어있을 때... 그냥 최신순으로 출력해주기
    public List<GetRankedEventResponse> findFallbackEvents(String category) {
        var conditions = DSL.noCondition();

        conditions = conditions.and(EVENTS.DELETED_AT.isNull());
        conditions = conditions.and(EVENTS.EVENT_STATUS.eq(String.valueOf(EventStatus.SCHEDULED)));

        if (category != null) {
            conditions = conditions.and(EVENTS.CATEGORY.eq(category));
        }

//        return dsl.select(
//                        EVENTS.ID,
//                        EVENTS.TITLE,
//                        EVENTS.DESCRIPTION,
//                        EVENTS.EVENT_STATUS,
//                        EVENTS.OPEN_DATE,
//                        EVENTS.END_DATE,
//                        EVENTS.CATEGORY,
//                        VENUES.LOCATION,
//                        VENUES.NAME)
//                .from(EVENTS)
//                .join(VENUES).on(VENUES.ID.eq(EVENTS.VENUE_ID))
//                .where(conditions)
//                .orderBy(EVENTS.OPEN_DATE.desc()) // 최신순
//                .limit(TOP_N)
//                .fetch(record -> new GetRankedEventResponse(
//                        record.get(EVENTS.CATEGORY),
//                        new GetEventResponse(
//                                record.get(EVENTS.TITLE),
//                                record.get(EVENTS.DESCRIPTION),
//                                record.get(VENUES.LOCATION),
//                                record.get(VENUES.NAME),
//                                record.get(EVENTS.EVENT_STATUS),
//                                record.get(EVENTS.OPEN_DATE),
//                                record.get(EVENTS.END_DATE)
//                        ),
//                        0L // 조회수 없으면 0
//                ));

        return dsl.select(
                        EVENTS.ID,
                        EVENTS.TITLE,
                        EVENTS.DESCRIPTION,
                        EVENTS.EVENT_STATUS,
                        EVENTS.OPEN_DATE,
                        EVENTS.END_DATE,
                        EVENTS.CATEGORY,
                        VENUES.LOCATION,
                        VENUES.NAME)
                .from(EVENTS)
                .join(VENUES).on(VENUES.ID.eq(EVENTS.VENUE_ID))
                .where(conditions)
                .orderBy(EVENTS.OPEN_DATE.asc())
                .limit(TOP_N)
                .fetch(record -> new GetRankedEventResponse(
                        record.get(EVENTS.CATEGORY),
                        new GetEventResponse(
                                record.get(EVENTS.ID),
                                record.get(EVENTS.TITLE),
                                record.get(EVENTS.DESCRIPTION),
                                record.get(VENUES.LOCATION),
                                record.get(VENUES.NAME),
                                record.get(EVENTS.EVENT_STATUS),
                                record.get(EVENTS.OPEN_DATE),
                                record.get(EVENTS.END_DATE)
                        ),
                        0L
                ));
    }
}