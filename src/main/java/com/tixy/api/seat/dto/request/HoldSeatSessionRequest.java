package com.tixy.api.seat.dto.request;

import jakarta.validation.constraints.NotNull;

public record HoldSeatSessionRequest(
        @NotNull
        Long eventSessionId,

        @NotNull
        Long seatId
) {
}
