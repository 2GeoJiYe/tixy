package com.tixy.api.order.controller;

import com.tixy.api.order.dto.response.CreateOrderResponse;
import com.tixy.api.order.service.OrderFacadeService;
import com.tixy.api.seat.dto.request.HoldSeatSessionRequest;
import com.tixy.core.dto.ApiResponse;
import com.tixy.core.security.dto.LoginUserInfoDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacadeService orderFacadeService;

    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> seatHoldLock(
            @AuthenticationPrincipal LoginUserInfoDto userInfo,
            @RequestBody @Valid HoldSeatSessionRequest holdSeatSessionRequest) {
        CreateOrderResponse response = orderFacadeService.order(holdSeatSessionRequest.eventSessionId(),holdSeatSessionRequest.seatIds(), userInfo.id());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/v1/pessimistic")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> seatHoldPessimisticLock(
            @AuthenticationPrincipal LoginUserInfoDto userInfo,
            @RequestBody @Valid HoldSeatSessionRequest holdSeatSessionRequest) {
        CreateOrderResponse response = orderFacadeService.orderPessimistic(holdSeatSessionRequest.eventSessionId(),holdSeatSessionRequest.seatIds(), userInfo.id());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/v1/nolock")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> seatHoldNoLock(
            @AuthenticationPrincipal LoginUserInfoDto userInfo,
            @RequestBody @Valid HoldSeatSessionRequest holdSeatSessionRequest) {
        CreateOrderResponse response = orderFacadeService.orderNoLock(holdSeatSessionRequest.eventSessionId(),holdSeatSessionRequest.seatIds(), userInfo.id());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
