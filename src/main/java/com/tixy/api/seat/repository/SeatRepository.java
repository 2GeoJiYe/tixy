package com.tixy.api.seat.repository;

import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findAllBySeatSectionId(Long seatSectionId);
    List<Seat> findAllBySeatStatus(SeatStatus status);
}
