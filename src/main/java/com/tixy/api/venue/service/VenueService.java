package com.tixy.api.venue.service;

import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.enums.VenueStatus;
import com.tixy.api.venue.repository.VenueRepository;
import com.tixy.core.exception.venue.VenueErrorCode;
import com.tixy.core.exception.venue.VenueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;

    public Venue saveVenue(String name) {
        Venue venue = Venue.builder()
                .name(name)
                .venueStatus(VenueStatus.ACTIVE)
                .build();

        venueRepository.save(venue);

        return venue;
    }

    public Venue findVenueById(Long id) {
        return venueRepository.findById(id).orElseThrow(
                () -> new VenueException(VenueErrorCode.VENUE_NOT_FOUND)
        );
    }
}
