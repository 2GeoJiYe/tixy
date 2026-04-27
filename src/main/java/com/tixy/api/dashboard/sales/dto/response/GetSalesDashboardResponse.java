package com.tixy.api.dashboard.sales.dto.response;

import java.util.List;

// 판매 속도 대시보드 전체 응답
public record GetSalesDashboardResponse(
        GetSalesSummaryResponse summary,    // 상단 요약 카드
        List<GetSessionSalesSpeedResponse> sessions,    // 회차별 판매 속도/랭킹
        List<GetSalesTrendResponse> trend   // 기간 추이 차트
) {
}
