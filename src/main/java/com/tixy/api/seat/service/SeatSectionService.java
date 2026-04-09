package com.tixy.api.seat.service;

import com.tixy.api.seat.dto.request.CreateSeatSectionRequest;
import com.tixy.api.seat.dto.response.SeatSectionResponse;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.mapper.SeatMapper;
import com.tixy.api.seat.repository.SeatSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SeatSectionService {
    private final SeatSectionRepository seatSectionRepository;
    private final SeatMapper seatMapper;

    // 내부용 서비스
    public SeatSectionResponse createSeatSection(CreateSeatSectionRequest createSeatSectionRequest) {
        SeatSection seatSection = SeatSection.builder()
                .venue(createSeatSectionRequest.venue())
                .sectionName(createSeatSectionRequest.sectionName())
                .grade(createSeatSectionRequest.grade())
                .build();
        return seatMapper.toSeatSectionResponse(seatSectionRepository.save(seatSection));
    }
}
