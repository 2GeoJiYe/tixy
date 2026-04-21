package com.tixy.api.seat.dto.response;

import java.util.List;

public record ActiveSeatSessionResponse(
        Long eventSessionId,
        List<Long> seatSessionIds
) {
}
