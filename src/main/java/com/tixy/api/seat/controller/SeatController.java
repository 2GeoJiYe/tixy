package com.tixy.api.seat.controller;

import com.tixy.api.seat.service.SeatSessionService;
import com.tixy.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatSessionService seatSessionService;

    @PostMapping("/v1/{eventId}/seat-sessions")
    public ResponseEntity<ApiResponse<Void>> createSeatSessions(@PathVariable Long eventId) {
        seatSessionService.createSeatSessions(eventId);
        return ResponseEntity.accepted().build();
    }
}
