package com.tixy.api.event.controller;

import com.tixy.api.event.dto.request.CreateEventRequest;
import com.tixy.api.event.dto.request.GetEventsRequest;
import com.tixy.api.event.dto.request.UpdateEventRequest;
import com.tixy.api.event.dto.response.CreateEventResponse;
import com.tixy.api.event.dto.response.DeleteEventResponse;
import com.tixy.api.event.dto.response.GetEventResponse;
import com.tixy.api.event.service.EventService;
import com.tixy.core.dto.ApiResponse;
import com.tixy.core.security.dto.LoginUserInfoDto;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
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
    public ResponseEntity<ApiResponse<GetEventResponse>> getOneEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal LoginUserInfoDto userInfo){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventService.findOne(eventId, userInfo)));
    }

    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<GetEventResponse>>> getEvents(
            @ModelAttribute GetEventsRequest request, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventService.findAll(request, pageable)));
    }

    @GetMapping("/v2")
    public ResponseEntity<ApiResponse<List<GetEventResponse>>> getEventsWithLocalCache(
            @ModelAttribute GetEventsRequest request, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventService.findAllV2(request, pageable)));
    }

    @GetMapping("/v3")
    public ResponseEntity<ApiResponse<List<GetEventResponse>>> getEventsWithRedisCache(
            @ModelAttribute GetEventsRequest request, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventService.findAllV3(request, pageable)));
    }

    @PutMapping("/v1/{eventId}")
    public ResponseEntity<ApiResponse<GetEventResponse>> updateEvent(
            @PathVariable Long eventId, @RequestBody @Valid UpdateEventRequest updateEventRequest){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventService.update(eventId, updateEventRequest)));
    }

    @DeleteMapping("/v1/{eventId}")
    public ResponseEntity<ApiResponse<DeleteEventResponse>> deleteEvent(
            @PathVariable Long eventId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventService.delete(eventId)));
    }
}
