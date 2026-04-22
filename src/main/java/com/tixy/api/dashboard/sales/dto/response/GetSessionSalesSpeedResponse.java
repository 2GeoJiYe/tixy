package com.tixy.api.dashboard.sales.dto.response;

import java.time.LocalDateTime;

// 회차별로 판매 속도랑 랭킹
public record GetSessionSalesSpeedResponse(
        Long eventId,
        String eventTitle,
        Long sessionId,
        String sessionName,
        LocalDateTime sessionOpenDate,
        Long sessionSeatCount,
        Long soldTicketCount,
        Long paidAmount,
        Long sold10m,
        Long sold30m,
        Long sold60m,
        Double sellThroughRate,         // 총 좌석 대비 판매된 티켓 비율
        Long remainingSeatCount         // 총 좌석 수에서 판매 티켓 수를 뺀 잔여 좌석 추정치
) {
}
