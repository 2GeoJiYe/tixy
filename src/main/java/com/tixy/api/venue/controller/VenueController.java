package com.tixy.api.venue.controller;

import com.tixy.api.venue.dto.request.CreateVenueRequest;
import com.tixy.api.venue.dto.response.CreateVenueResponse;
import com.tixy.api.venue.service.VenueFacadeService;
import com.tixy.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueFacadeService venueService;

    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<CreateVenueResponse>> createVenue(@RequestBody @Valid CreateVenueRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createVenue(request)));
    }
}
