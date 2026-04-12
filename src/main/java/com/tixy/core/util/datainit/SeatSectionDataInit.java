package com.tixy.core.util.datainit;

import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.enums.Grade;
import com.tixy.api.seat.repository.SeatSectionRepository;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class SeatSectionDataInit {
    private final SeatSectionRepository seatSectionRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public void initSeatSections() {
        if (seatSectionRepository.count() > 0) return;

        List<Venue> venues = venueRepository.findAll();
        Random random = new Random(42);
        List<SeatSection> seatSections = new ArrayList<>();

        List<Grade> grades = new ArrayList<>(List.of(Grade.values()));

        for (Venue venue : venues) {
            int count = random.nextInt(5) + 1; // 1~5개
            Collections.shuffle(grades, random);

            for (int i = 0; i < count; i++) {
                SeatSection seatSection = SeatSection.builder()
                        .venue(venue)
                        .grade(grades.get(i))
                        .build();
                seatSections.add(seatSection);
            }
        }

        seatSectionRepository.saveAll(seatSections);
        System.out.println("seatSection dummy data " + seatSections.size() + "개 저장 완료!");
    }
}
