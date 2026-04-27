package com.tixy.api.dashboard.sales.service;

import com.tixy.api.dashboard.sales.dto.request.GetSalesDashboardRequest;
import com.tixy.api.dashboard.sales.dto.response.GetSalesDashboardResponse;
import com.tixy.api.dashboard.sales.dto.response.GetSalesSummaryResponse;
import com.tixy.api.dashboard.sales.dto.response.GetSalesTrendResponse;
import com.tixy.api.dashboard.sales.dto.response.GetSessionSalesSpeedResponse;
import com.tixy.api.dashboard.sales.enums.SalesTrendGranularity;
import com.tixy.api.dashboard.sales.repository.EventSalesDashboardQueryRepository;
import com.tixy.core.exception.CommonErrorCode;
import com.tixy.core.exception.event.EventServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// 판매 속도 대시보드 응답 서비스 기간 검증이랑 응답 조립
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSalesDashboardService {

    private static final long MAX_RANGE_DAYS = 31;
    private static final long MAX_HOURLY_RANGE_DAYS = 3;

    private final EventSalesDashboardQueryRepository eventSalesDashboardQueryRepository;

    // 대시보드 조회 요청을 받아서 요약, 회차별 판매 속도, 추이 데이터 조합
    public GetSalesDashboardResponse getSalesDashboard(GetSalesDashboardRequest request){
        SalesTrendGranularity granularity =
                request.granularity() == null ? SalesTrendGranularity.DAY : request.granularity();

        validateRange(request.from(), request.to(), granularity);

        GetSalesSummaryResponse summary =
                eventSalesDashboardQueryRepository.findSalesSummary(
                        request.from(),
                        request.to(),
                        request.eventId()
                );

        List<GetSessionSalesSpeedResponse> sessions =
                eventSalesDashboardQueryRepository.findSessionSalesSpeed(
                        request.from(),
                        request.to(),
                        request.eventId()
                );

        List<GetSalesTrendResponse> trend =
                eventSalesDashboardQueryRepository.findSalesTrend(
                        request.from(),
                        request.to(),
                        granularity,
                        request.eventId()
                );

        return new GetSalesDashboardResponse(summary, sessions, trend);
    }

    // 과도한 기간 조회를 막기 위해서 방어 로직을 둔다
    // 판매 대시보드는 집계 쿼리 비중이 커서 너무 넓은 기간을 허용하면 응답 무거워짐.... 시간 단위 조회는 엄격하게 제한
    private void validateRange(
            LocalDateTime from,
            LocalDateTime to,
            SalesTrendGranularity granularity
    ) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new EventServiceException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        long daysBetween = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate());

        if (daysBetween > MAX_RANGE_DAYS) {
            throw new EventServiceException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        if (granularity == SalesTrendGranularity.HOUR && daysBetween > MAX_HOURLY_RANGE_DAYS) {
            throw new EventServiceException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
