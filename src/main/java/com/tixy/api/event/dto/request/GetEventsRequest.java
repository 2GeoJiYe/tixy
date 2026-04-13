package com.tixy.api.event.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record GetEventsRequest(
        Boolean reservePossible,    // 한 세션이라도 예매가능 상태잊니
        List<String> area,          // 지역 (여러개 선택 가능)
        List<String> category,      // 카테고리 (여러개 선택 가능)
        LocalDateTime startDate,    // 시작일
        LocalDateTime endDate,      // 종료일
        String keyword,             // 제목 또는 설명에 해당 키워드가 포함되었는지
        Long startPrice,            // 최저 가격
        Long endPrice               // 최대 가격
) {
}
