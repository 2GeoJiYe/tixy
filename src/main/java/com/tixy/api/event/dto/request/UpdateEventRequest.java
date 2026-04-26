package com.tixy.api.event.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateEventRequest(
        Long venueId,

        @Size(min = 1, message = "제목은 비어있을 수 없습니다.")
        String title,

        @Size(min = 1, message = "설명은 비어있을 수 없습니다.")
        String description
) {
        public UpdateEventRequest {
                if (title != null) {
                        title = title.trim();
                }
                if (description != null) {
                        description = description.trim();
                }
        }
}
