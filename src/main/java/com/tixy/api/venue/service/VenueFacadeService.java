package com.tixy.api.venue.service;

import com.tixy.api.seat.dto.request.CreateSeatSectionRequest;
import com.tixy.api.seat.dto.response.SeatSectionResponse;
import com.tixy.api.seat.service.SeatSectionService;
import com.tixy.api.seat.service.SeatService;
import com.tixy.api.venue.dto.request.CreateVenueRequest;
import com.tixy.api.venue.dto.request.SectionItem;
import com.tixy.api.venue.dto.response.CreateVenueResponse;
import com.tixy.api.venue.dto.response.VenueResponse;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.mapper.VenueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenueFacadeService {

    private final VenueService venueService;
    private final SeatSectionService seatSectionService;
    private final SeatService seatService;
    private final VenueMapper venueMapper;

    @Transactional
    public CreateVenueResponse createVenue(CreateVenueRequest request) {
        // 공연장 저장
        Venue venue = venueService.saveVenue(request.name());

        // 구역, 자리 저장
        for (SectionItem section : request.sections()) {
            // 구역 DTO 생성하기
            CreateSeatSectionRequest createSeatSectionRequest = new CreateSeatSectionRequest(
                    venue,
                    section.sectionName(),
                    section.grade()
            );

            // 구역 저장하고
            SeatSectionResponse response = seatSectionService.createSeatSection(createSeatSectionRequest);
            //해당 구역에 자리 정보 저장
            seatService.createSeats(response.id(), section.rowLabels());
        }

        return new CreateVenueResponse(
                venue.getName()
        );
    }

    public VenueResponse getVenueById(Long id) {
        Venue venue = venueService.findVenueById(id);
        return venueMapper.toVenueResponse(venue);
    }
}
