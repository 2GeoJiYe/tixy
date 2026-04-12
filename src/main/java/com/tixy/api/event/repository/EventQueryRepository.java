package com.tixy.api.event.repository;

import com.tixy.api.ticket.enums.TicketTypeStatus;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.tixy.jooq.tables.Events.EVENTS;
import static com.tixy.jooq.tables.EventSessions.EVENT_SESSIONS;
import static com.tixy.jooq.tables.TicketTypes.TICKET_TYPES;


@Repository
@RequiredArgsConstructor
public class EventQueryRepository {

    private final DSLContext dsl;

//     하나라도 PENDING 상태가 아닌 ticket type 이 있다면 수정,삭제 불가능
//     true -> EventStatus 는 모두 SCHEDULED (OPEN 된 이상 무조건 예매가 시작되었음)
//     false -> 모든 EventStatus 가능
    public boolean existsNonPendingTicketTypeByEventId(Long eventId){
        return dsl.fetchExists(
                dsl.selectOne()
                .from(EVENTS)
                .join(EVENT_SESSIONS).on(EVENTS.ID.eq(EVENT_SESSIONS.EVENT_ID))
                .join(TICKET_TYPES).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                .where(EVENTS.ID.eq(eventId))
                .and(TICKET_TYPES.TICKET_TYPE_STATUS.ne(String.valueOf(TicketTypeStatus.PENDING))));
    }
}
