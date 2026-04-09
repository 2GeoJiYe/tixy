package com.tixy.api.venue.controller;

import com.tixy.api.venue.dto.request.CreateVenueRequest;
import com.tixy.api.venue.dto.response.CreateVenueResponse;
import com.tixy.api.venue.service.VenueFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venue")
@RequiredArgsConstructor
public class VenueController {

    private final VenueFacadeService venueService;

    @PostMapping("/v1")
    public ResponseEntity<CreateVenueResponse> createVenue(@RequestBody @Valid CreateVenueRequest request){
        return ResponseEntity.ok(venueService.createVenue(request));
    }
}
