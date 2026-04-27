package com.tixy.api.dashboard.sales.dto.response;

// 상단에 KPI 카드에 필요한 요약
public record GetSalesSummaryResponse(
        Long soldTicketCount,   // 기간 내 판매 티켓 수
        Long paidAmount,        // 실입금 기준 매출
        Integer paymentCount,   // 결제 성공 건수
        Integer sessionCount    // 실제 판매가 발생한 회차 수
) {
}
