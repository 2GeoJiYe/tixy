package com.tixy.api.venue.dto.request;

import com.tixy.api.seat.enums.Grade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SectionItem(
        @NotBlank
        String sectionName,
        @NotNull
        Grade grade,
        @NotEmpty
        List<String> rowLabels
) {}