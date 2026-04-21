package com.tixy.api.dashboard.sales.dto.response;

import java.util.List;

public record GetSalesDashboardResponse(
        GetSalesSummaryResponse summary,
        List<GetSessionSalesSpeedResponse> sessions,
        List<GetSalesTrendResponse> trend
) {
}
