package com.tixy.core.util.datainit;

import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.entity.SeatSection;
import com.tixy.api.seat.enums.SeatStatus;
import com.tixy.api.seat.repository.SeatRepository;
import com.tixy.api.seat.repository.SeatSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class SeatDataInit {
    private final SeatRepository seatRepository;
    private final SeatSectionRepository seatSectionRepository;

    @Transactional
    public void initSeat() {
        if (seatRepository.count() > 0) return;

        List<SeatSection> seatSections = seatSectionRepository.findAll();
        Random random = new Random(42);
        List<Seat> seats = new ArrayList<>();

        String[] rowLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};

        for (SeatSection seatSection : seatSections) {
            int rowCount = random.nextInt(3) + 3; // 3~5행

            for (int row = 0; row < rowCount; row++) {
                int seatCount = random.nextInt(5) + 6; // 행당 6~10석

                for (int col = 1; col <= seatCount; col++) {
                    Seat seat = Seat.builder()
                            .seatStatus(SeatStatus.ACTIVE)
                            .seatSection(seatSection)
                            .rowLabel(rowLetters[row] + col) // ex) A1, A2, B1, B3
                            .build();
                    seats.add(seat);
                }
            }
        }

        seatRepository.saveAll(seats);
        System.out.println("seats dummy data " + seats.size() + "개 저장 완료!");
    }
}
