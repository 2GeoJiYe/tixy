package com.tixy.api.event.controller;

import com.tixy.api.event.dto.response.GetEventSessionsResponse;
import com.tixy.api.event.dto.response.GetOneEventSessionResponse;
import com.tixy.api.event.service.EventSessionService;
import com.tixy.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event/v1/{eventId}/schedules")
@RequiredArgsConstructor
public class EventSessionController {
    private final EventSessionService eventSessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetEventSessionsResponse>>> getEventSessions(
            @PathVariable Long eventId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(eventSessionService.findAll(eventId, pageable)));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<GetOneEventSessionResponse>> getOneEventSession(
            @PathVariable Long eventId, @PathVariable Long scheduleId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(eventSessionService.findOne(eventId, scheduleId)));
    }
}
