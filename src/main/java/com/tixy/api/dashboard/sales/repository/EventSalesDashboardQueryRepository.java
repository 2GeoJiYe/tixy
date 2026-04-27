package com.tixy.api.dashboard.sales.repository;

import com.tixy.api.dashboard.sales.dto.response.GetSalesSummaryResponse;
import com.tixy.api.dashboard.sales.dto.response.GetSalesTrendResponse;
import com.tixy.api.dashboard.sales.dto.response.GetSessionSalesSpeedResponse;
import com.tixy.api.dashboard.sales.enums.SalesTrendGranularity;
import com.tixy.api.payment.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.tixy.jooq.tixy.tables.Events.EVENTS;
import static com.tixy.jooq.tixy.tables.EventSessions.EVENT_SESSIONS;
import static com.tixy.jooq.tixy.tables.Orders.ORDERS;
import static com.tixy.jooq.tixy.tables.Payments.PAYMENTS;
import static com.tixy.jooq.tixy.tables.TicketTypes.TICKET_TYPES;

// 판매 속도 대시보드 전용 JOOQ 조회 repository
// 주문, 결제, 티켓 타입, 회차, 공연 데이터를 조인해서 대시보드 화면에 필요한 집계 결과만 반환
@Repository
@RequiredArgsConstructor
public class EventSalesDashboardQueryRepository {

    private final DSLContext dsl;

    // 상단 KPI 카드용 요약 지표 조회
    // 판매 기준은 payment_status = SUCCESS로 잡음
    public GetSalesSummaryResponse findSalesSummary(
            LocalDateTime from,
            LocalDateTime to,
            Long eventId
    ) {
        Field<Long> zero = DSL.inline(0L);

        Field<Long> soldTicketCount = DSL.coalesce(
                DSL.sum(ORDERS.TICKET_COUNT).cast(Long.class),
                zero
        ).as("sold_ticket_count");

        Field<Long> paidAmount = DSL.coalesce(
                DSL.sum(PAYMENTS.PAY_AMOUNT).cast(Long.class),
                zero
        ).as("paid_amount");

        Field<Integer> paymentCount = DSL.count(PAYMENTS.ID).as("payment_count");
        Field<Integer> sessionCount = DSL.countDistinct(EVENT_SESSIONS.ID).as("session_count");

        return dsl.select(soldTicketCount, paidAmount, paymentCount, sessionCount)
                .from(PAYMENTS)
                .join(ORDERS).on(PAYMENTS.ORDER_ID.eq(ORDERS.ID))
                .join(TICKET_TYPES).on(ORDERS.TICKET_TYPE_ID.eq(TICKET_TYPES.ID))
                .join(EVENT_SESSIONS).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                .join(EVENTS).on(EVENT_SESSIONS.EVENT_ID.eq(EVENTS.ID))
                .where(baseCondition(from, to, eventId))
                .fetchOne(record -> new GetSalesSummaryResponse(
                        record.get(soldTicketCount),
                        record.get(paidAmount),
                        record.get(paymentCount),
                        record.get(sessionCount)
                ));
    }

    // 회차별로 판매 속도랑 랭킹 데이터를 조회함
    // 회차 오픈 시각 대비 결제 성공 시각 차이를 계산해서 오픈 후 10분, 30분, 60분 판매량을 조건부 집계로 구함
    public List<GetSessionSalesSpeedResponse> findSessionSalesSpeed(
            LocalDateTime from,
            LocalDateTime to,
            Long eventId
    ) {
        // 오픈 시각 대비 결제 완료 시각 차이를 분 단위로 계산함
        Field<Integer> minutesFromOpen = DSL.field(
                "timestampdiff(minute, {0}, {1})",
                Integer.class,
                EVENT_SESSIONS.SESSION_OPEN_DATE,
                PAYMENTS.DEPOSIT_AT
        );

        // 대시보드에서는 null 대신 0으로 보여 주는 것이 자연스러워서 기본값 맞춤
        Field<Long> zero = DSL.inline(0L);

        Field<Long> soldTicketCount = DSL.coalesce(
                DSL.sum(ORDERS.TICKET_COUNT).cast(Long.class),
                zero
        ).as("sold_ticket_count");

        Field<Long> paidAmount = DSL.coalesce(
                DSL.sum(PAYMENTS.PAY_AMOUNT).cast(Long.class),
                zero
        ).as("paid_amount");

        // 회차별 판매 속도 카드는 누적 판매량이랑 초기 반응 속도를 함께 보여 줌
        Field<Long> sold10m = DSL.coalesce(
                DSL.sum(
                        DSL.when(minutesFromOpen.le(10), ORDERS.TICKET_COUNT.cast(Long.class))
                                .otherwise(DSL.inline(0L))
                ).cast(Long.class),
                zero
        ).as("sold_10m");

        Field<Long> sold30m = DSL.coalesce(
                DSL.sum(
                        DSL.when(minutesFromOpen.le(30), ORDERS.TICKET_COUNT.cast(Long.class))
                                .otherwise(DSL.inline(0L))
                ).cast(Long.class),
                zero
        ).as("sold_30m");

        Field<Long> sold60m = DSL.coalesce(
                DSL.sum(
                        DSL.when(minutesFromOpen.le(60), ORDERS.TICKET_COUNT.cast(Long.class))
                                .otherwise(DSL.inline(0L))
                ).cast(Long.class),
                zero
        ).as("sold_60m");

        return dsl.select(
                        EVENTS.ID,
                        EVENTS.TITLE,
                        EVENT_SESSIONS.ID,
                        EVENT_SESSIONS.SESSION,
                        EVENT_SESSIONS.SESSION_OPEN_DATE,
                        EVENT_SESSIONS.SESSION_SEAT_COUNT,
                        soldTicketCount,
                        paidAmount,
                        sold10m,
                        sold30m,
                        sold60m
                )
                .from(PAYMENTS)
                .join(ORDERS).on(PAYMENTS.ORDER_ID.eq(ORDERS.ID))
                .join(TICKET_TYPES).on(ORDERS.TICKET_TYPE_ID.eq(TICKET_TYPES.ID))
                .join(EVENT_SESSIONS).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                .join(EVENTS).on(EVENT_SESSIONS.EVENT_ID.eq(EVENTS.ID))
                .where(baseCondition(from, to, eventId))
                .groupBy(
                        EVENTS.ID,
                        EVENTS.TITLE,
                        EVENT_SESSIONS.ID,
                        EVENT_SESSIONS.SESSION,
                        EVENT_SESSIONS.SESSION_OPEN_DATE,
                        EVENT_SESSIONS.SESSION_SEAT_COUNT
                )
                .orderBy(
                        paidAmount.desc(),
                        soldTicketCount.desc(),
                        EVENT_SESSIONS.SESSION_OPEN_DATE.asc()
                )
                .fetch(record -> {
                    Long seatCount = record.get(EVENT_SESSIONS.SESSION_SEAT_COUNT);
                    Long soldCount = record.get(soldTicketCount);

                    return new GetSessionSalesSpeedResponse(
                            record.get(EVENTS.ID),
                            record.get(EVENTS.TITLE),
                            record.get(EVENT_SESSIONS.ID),
                            record.get(EVENT_SESSIONS.SESSION),
                            record.get(EVENT_SESSIONS.SESSION_OPEN_DATE),
                            seatCount,
                            soldCount,
                            record.get(paidAmount),
                            record.get(sold10m),
                            record.get(sold30m),
                            record.get(sold60m),
                            calculateSellThroughRate(soldCount, seatCount),
                            calculateRemainingSeatCount(soldCount, seatCount)
                    );
                });
    }

    // 기간 추이 차트용 집계 데이터 조회
    // granularity에 따라서 일별 또는 시간별 버킷으로 묶어서 판매 티켓 수, 매출, 결제 성공 건수 집계
    public List<GetSalesTrendResponse> findSalesTrend(
            LocalDateTime from,
            LocalDateTime to,
            SalesTrendGranularity granularity,
            Long eventId
    ) {
        Field<String> bucket = createTrendBucket(granularity);
        Field<Long> zero = DSL.inline(0L);

        Field<Long> soldTicketCount = DSL.coalesce(
                DSL.sum(ORDERS.TICKET_COUNT).cast(Long.class),
                zero
        ).as("sold_ticket_count");

        Field<Long> paidAmount = DSL.coalesce(
                DSL.sum(PAYMENTS.PAY_AMOUNT).cast(Long.class),
                zero
        ).as("paid_amount");

        Field<Integer> paymentCount = DSL.count(PAYMENTS.ID).as("payment_count");

        return dsl.select(bucket, soldTicketCount, paidAmount, paymentCount)
                .from(PAYMENTS)
                .join(ORDERS).on(PAYMENTS.ORDER_ID.eq(ORDERS.ID))
                .join(TICKET_TYPES).on(ORDERS.TICKET_TYPE_ID.eq(TICKET_TYPES.ID))
                .join(EVENT_SESSIONS).on(TICKET_TYPES.EVENT_SESSION_ID.eq(EVENT_SESSIONS.ID))
                .join(EVENTS).on(EVENT_SESSIONS.EVENT_ID.eq(EVENTS.ID))
                .where(baseCondition(from, to, eventId))
                .groupBy(bucket)
                .orderBy(bucket.asc())
                .fetch(record -> new GetSalesTrendResponse(
                        record.get(bucket),
                        record.get(soldTicketCount),
                        record.get(paidAmount),
                        record.get(paymentCount)
                ));
    }

    private Condition baseCondition(LocalDateTime from, LocalDateTime to, Long eventId) {
        Condition condition = PAYMENTS.PAYMENT_STATUS.eq(PaymentStatus.SUCCESS.name())
                .and(PAYMENTS.DEPOSIT_AT.between(from, to))
                .and(EVENTS.DELETED_AT.isNull());           // 삭제된 공연은 운영 화면 집계에서 제외

        if (eventId != null) {
            condition = condition.and(EVENTS.ID.eq(eventId));
        }

        return condition;
    }

    private Field<String> createTrendBucket(SalesTrendGranularity granularity) {
        if (granularity == SalesTrendGranularity.HOUR) {
            return DSL.field(
                    "date_format({0}, '%Y-%m-%d %H:00:00')",
                    SQLDataType.VARCHAR(19),
                    PAYMENTS.DEPOSIT_AT
            ).as("bucket");
        }

        return DSL.field(
                "date_format({0}, '%Y-%m-%d')",
                SQLDataType.VARCHAR(10),
                PAYMENTS.DEPOSIT_AT
        ).as("bucket");
    }

    private Double calculateSellThroughRate(Long soldTicketCount, Long sessionSeatCount) {
        if (soldTicketCount == null || sessionSeatCount == null || sessionSeatCount == 0L) {
            return 0.0;
        }

        double rate = (soldTicketCount.doubleValue() / sessionSeatCount.doubleValue()) * 100.0;
        return Math.round(rate * 100) / 100.0;
    }

    private Long calculateRemainingSeatCount(Long soldTicketCount, Long sessionSeatCount) {
        if (soldTicketCount == null || sessionSeatCount == null) {
            return 0L;
        }

        long remaining = sessionSeatCount - soldTicketCount;
        return Math.max(remaining, 0L);
    }
}
