package com.tixy.api.event.repository;

import com.tixy.api.event.dto.request.GetEventsRequest;
import com.tixy.api.event.dto.response.GetEventResponse;
import com.tixy.api.event.enums.EventSessionStatus;
import com.tixy.api.ticket.enums.TicketTypeStatus;
import com.tixy.api.venue.enums.Location;
import com.tixy.jooq.tables.Venues;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tixy.jooq.Tables.VENUES;
import static com.tixy.jooq.tables.Events.EVENTS;
import static com.tixy.jooq.tables.EventSessions.EVENT_SESSIONS;
import static com.tixy.jooq.tables.TicketTypes.TICKET_TYPES;


@Repository
@RequiredArgsConstructor
public class EventQueryRepository {

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
                    EVENT_SESSIONS.STATUS.ne(String.valueOf(EventSessionStatus.CLOSED))
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
                .join(EVENT_SESSIONS).on(EVENTS.ID.eq(EVENT_SESSIONS.EVENT_ID))
                .join(TICKET_TYPES).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                .join(VENUES).on(VENUES.ID.eq(EVENTS.VENUE_ID))
                .where(conditions);

        // 전체 count (페이징용)
        int total = dsl.fetchCount(query);

        // 실제 데이터 조회
//        List<GetEventResponse> results = query
//                .orderBy(EVENTS.OPEN_DATE.asc())
//                .limit(pageable.getPageSize())
//                .offset(pageable.getOffset())
//                .fetchInto(GetEventResponse.class);
        List<GetEventResponse> results = query
                .orderBy(EVENTS.OPEN_DATE.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(record -> new GetEventResponse(
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
}
