package com.tixy.api.seat.service;

import com.tixy.api.seat.dto.request.CreateSeatSectionRequest;
import com.tixy.api.seat.dto.response.SeatSectionResponse;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.mapper.SeatMapper;
import com.tixy.api.seat.repository.SeatSectionRepository;
import com.tixy.core.exception.seat.SeatErrorCode;
import com.tixy.core.exception.seat.SeatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


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

    public SeatSection getBySeatSectionId(Long seatSectionId) {
        return seatSectionRepository.findById(seatSectionId).orElseThrow(
                () -> new SeatException(SeatErrorCode.SEAT_SECTION_NOT_FOUND)
        );
    }

    public List<SeatSection> getAllByVenueId(Long venueId) {
        return seatSectionRepository.findAllByVenueId(venueId);
    }
}
