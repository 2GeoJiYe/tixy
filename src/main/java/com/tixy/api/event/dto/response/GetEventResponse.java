package com.tixy.api.event.dto.response;

import com.tixy.api.event.entity.Event;

import java.time.LocalDateTime;

public record GetEventResponse (
        String title,
        String description,
        String location,
        String venue,
        String eventStatus,
        LocalDateTime openDate,
        LocalDateTime endDate
) {
    public static GetEventResponse from(Event event) {
        return new GetEventResponse(
                event.getTitle(),
                event.getDescription(),
                event.getVenue().getLocation().name(),
                event.getVenue().getName(),
                event.getEventStatus().getStatus(),
                event.getOpenDate(),
                event.getEndDate()
        );
    }
}
