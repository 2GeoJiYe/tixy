package com.tixy.api.seat.dto.request;

import com.tixy.api.seat.enums.Grade;
import com.tixy.api.venue.entity.Venue;

public record CreateSeatSectionRequest(
        Venue venue,
        String sectionName,
        Grade grade
) {
}
