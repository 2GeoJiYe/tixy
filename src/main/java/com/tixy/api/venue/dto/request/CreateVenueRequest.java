package com.tixy.api.venue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateVenueRequest(
        @NotBlank
        String name,
        @NotEmpty
        List<SectionItem> sections
) {
}
