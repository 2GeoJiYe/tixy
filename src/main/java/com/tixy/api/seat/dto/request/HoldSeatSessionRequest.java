package com.tixy.api.seat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HoldSeatSessionRequest(
        @NotNull
        Long eventSessionId,

        @NotNull
        @Size(max = 5, message = "좌석은 최대 5개까지 선택할 수 있습니다.")
        List<Long> seatIds
) {
}
