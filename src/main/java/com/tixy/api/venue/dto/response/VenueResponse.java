package com.tixy.api.venue.dto.response;

import com.tixy.api.venue.enums.VenueStatus;

public record VenueResponse(
        String name,
        VenueStatus venueStatus
) {
}
