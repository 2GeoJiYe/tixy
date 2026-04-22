package com.tixy.api.dashboard.sales.dto.response;

// 기간 추이 차트용
public record GetSalesTrendResponse(
        String bucket,              // 일별 또는 시간별 집계 구간
        Long soldTicketCount,
        Long paidAmount,
        Integer paymentCount
) {
}
