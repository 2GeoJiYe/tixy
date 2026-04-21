package com.tixy.api.dashboard.sales.dto.response;

import java.time.LocalDateTime;

public record GetSessionSalesSpeedResponse(
        Long eventId,
        String eventTittle,
        Long sessionId,
        String sessionName,
        LocalDateTime sessionOpenDate,
        Long sessionSeatCount,
        Long paidAmount,
        Long sold10m,
        Long sold30m,
        Long sold60m,
        Double sellThroughRate,
        Long remainingSeatCount
) {
}
