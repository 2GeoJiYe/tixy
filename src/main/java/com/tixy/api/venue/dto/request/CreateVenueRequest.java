package com.tixy.api.venue.dto.request;

import com.tixy.api.venue.enums.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateVenueRequest(
        @NotBlank
        String name,

        @NotNull
        Location location,

        @NotNull
        Long totalSeatCount,

        @NotEmpty
        List<SectionItem> sections
) {
}
