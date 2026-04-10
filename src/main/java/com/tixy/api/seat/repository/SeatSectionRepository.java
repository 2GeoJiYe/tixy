package com.tixy.api.seat.repository;

import com.tixy.api.seat.entity.SeatSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatSectionRepository extends JpaRepository<SeatSection, Long> {

    List<SeatSection> findAllByVenueId(Long venueId);
}
