package com.tixy.api.dashboard.sales.dto.request;

import com.tixy.api.dashboard.sales.enums.SalesTrendGranularity;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

// 판매 속도 대시보드 조회 조건
// 1. from/to는 조회 기간
// 2. granularity는 추이 차트 버킷 단위
// 3. eventId는 특정 공연만 보고 싶을 때 사용하는 선택 조건
public record GetSalesDashboardRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to,

        SalesTrendGranularity granularity,
        Long eventId
) {
}
