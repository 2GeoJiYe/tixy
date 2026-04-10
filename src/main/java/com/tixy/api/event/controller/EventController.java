package com.tixy.api.event.controller;

import com.tixy.api.event.dto.request.CreateEventRequest;
import com.tixy.api.event.dto.response.CreateEventResponse;
import com.tixy.api.event.dto.response.GetEventResponse;
import com.tixy.api.event.service.EventService;
import com.tixy.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<CreateEventResponse>> createEvent(@RequestBody @Valid CreateEventRequest createEventRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(eventService.save(createEventRequest)));
    }

    @GetMapping("/v1/{eventId}")
    public ResponseEntity<ApiResponse<GetEventResponse>> getOneEvent(@PathVariable Long eventId){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(eventService.findOne(eventId)));
    }
}
