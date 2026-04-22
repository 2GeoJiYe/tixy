package com.tixy.api.dashboard.sales.controller;

import com.tixy.api.dashboard.sales.dto.request.GetSalesDashboardRequest;
import com.tixy.api.dashboard.sales.dto.response.GetSalesDashboardResponse;
import com.tixy.api.dashboard.sales.service.EventSalesDashboardService;
import com.tixy.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 대시보드 조회 api
// 회차별 판매 속도, 요약 지표, 기간 추이를 내려줌
// 대시보드 화면이 카드별로 api를 여러 번 호출하지 않게 묶음
@RestController
@RequestMapping("/api/dashboard/v1/sales")
@RequiredArgsConstructor
public class EventSalesDashboardController {

    private final EventSalesDashboardService eventSalesDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<GetSalesDashboardResponse>> getSalesDashboard(
            @ModelAttribute GetSalesDashboardRequest request
            ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventSalesDashboardService.getSalesDashboard(request)));
    }
}
