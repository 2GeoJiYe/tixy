package com.tixy.api.event.dto.response;

import com.tixy.api.event.entity.Event;

public record DeleteEventResponse (
        Long id,
        String title,
        String eventStatus
){
    public static DeleteEventResponse from(Event event){
        return new DeleteEventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventStatus().getStatus()
        );
    }
}
