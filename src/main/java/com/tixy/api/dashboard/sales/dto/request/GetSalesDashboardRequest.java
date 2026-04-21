package com.tixy.api.dashboard.sales.dto.request;

import com.tixy.api.dashboard.sales.enums.SalesTrendGranularity;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record GetSalesDashboardRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to,

        SalesTrendGranularity granularity,
        Long eventId
) {
}
