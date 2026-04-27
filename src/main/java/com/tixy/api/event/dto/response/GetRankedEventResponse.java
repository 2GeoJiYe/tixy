package com.tixy.api.event.dto.response;

public record GetRankedEventResponse(
        String referenceCategory,
        GetEventResponse eventInfo,
        Long viewScore
) {
}
