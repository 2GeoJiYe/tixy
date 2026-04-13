package com.tixy.api.seat.service;

import com.tixy.api.seat.entity.Seat;
import com.tixy.api.seat.enums.SeatStatus;
import com.tixy.api.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final JdbcTemplate jdbcTemplate;
    private final SeatRepository seatRepository;

    // 내부용 서비스
    public void createSeats(Long sectionId, List<String> rowLabels) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.batchUpdate(
                "INSERT INTO seats (seat_section_id, seat_status, row_label , created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                rowLabels, // 사용할 데이터
                rowLabels.size(), // 배치 사이즈
                (ps, item) -> {
                    ps.setLong(1, sectionId); // sectionid 설정 해주기
                    ps.setString(2, SeatStatus.ACTIVE.name()); // 상태
                    ps.setString(3, item); // 자리 string
                    ps.setTimestamp(4, Timestamp.valueOf(now));
                    ps.setTimestamp(5, Timestamp.valueOf(now));
                }
        );
    }

    public List<Seat> getAllBySectionId(Long sectionId) {
        return seatRepository.findAllBySeatSectionId(sectionId);
    }
}
