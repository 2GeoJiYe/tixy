package com.tixy.api.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record GetEventsRequest(
        Boolean reservePossible,    // 한 세션이라도 예매가능 상태잊니
        List<String> area,          // 지역 (여러개 선택 가능)
        List<String> category,      // 카테고리 (여러개 선택 가능)
        LocalDateTime startDate,    // 시작일
        LocalDateTime endDate,      // 종료일
        @Size(min=2, message = "keyword 는 최소 2글자 입니다.")
        String keyword,             // 제목 또는 설명에 해당 키워드가 포함되었는지
        Long startPrice,            // 최저 가격
        Long endPrice               // 최대 가격
) {
    public GetEventsRequest{
        // 어노테이션 바인딩 이후 trim 실행 되고 @Valid 검증 -> trim 이후 @Size 체크가 됨
        if (keyword != null) {
            keyword = keyword.trim();
        }
    }
}
