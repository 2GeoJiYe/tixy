package com.tixy.api.seat.controller;

import com.tixy.api.seat.dto.request.HoldSeatSessionRequest;
import com.tixy.api.seat.service.SeatHoldService;
import com.tixy.api.seat.service.SeatSessionService;
import com.tixy.core.dto.ApiResponse;
import com.tixy.core.security.dto.LoginUserInfoDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatSessionService seatSessionService;
    private final SeatHoldService seatHoldService;

    @PostMapping("/v1/{eventId}/seat-sessions")
    public ResponseEntity<ApiResponse<Void>> createSeatSessions(@PathVariable Long eventId) {
        seatSessionService.createSeatSessions(eventId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/v1/seat-hold/no-lock")
    public ResponseEntity<ApiResponse<Void>> seatHoldNoLock(
            @AuthenticationPrincipal LoginUserInfoDto userInfo,
            @RequestBody @Valid HoldSeatSessionRequest holdSeatSessionRequest) {
        seatHoldService.seatHoldNoLock(holdSeatSessionRequest.eventSessionId(),holdSeatSessionRequest.seatIds(), userInfo.id());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/seat-hold")
    public ResponseEntity<ApiResponse<Void>> seatHold(
            @AuthenticationPrincipal LoginUserInfoDto userInfo,
            @RequestBody @Valid HoldSeatSessionRequest holdSeatSessionRequest) {
        seatHoldService.seatHold(holdSeatSessionRequest.eventSessionId(),holdSeatSessionRequest.seatIds(), userInfo.id());
        return ResponseEntity.ok().build();
    }
}
