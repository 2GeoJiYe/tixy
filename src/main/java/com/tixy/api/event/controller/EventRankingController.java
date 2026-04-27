package com.tixy.api.event.controller;

import com.tixy.api.event.dto.response.GetRankedEventResponse;
import com.tixy.api.event.service.EventRankingService;
import com.tixy.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tixy/api/events/v1/popular")
@RequiredArgsConstructor
public class EventRankingController {

    private final EventRankingService eventRankingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetRankedEventResponse>>> getRankedEvent(
            @RequestParam(required = false) String category
    ) throws InterruptedException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(eventRankingService.findPopularEvents(category)));
    }
}
