package com.tixy.core.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
// @Disabled  // 실행할 때는 주석 처리!
class TestDataGenerator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void generateTestData() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Venue 100개
        String venueSql = "INSERT INTO venues (name, venue_status, location, total_seat_count, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        for (int v = 1; v <= 100; v++) {
            String[] locations = {"SEOUL", "BUSAN", "GYEONGGI", "JEJU", "GYEONGNAM"};
            jdbcTemplate.update(venueSql,
                    "Venue_" + v, "ACTIVE", locations[v % 5], 1000 + v * 10, now, now
            );
        }
        System.out.println("Venue 100개 완료");

        // 2. SeatSection (Venue당 3개씩 = 300개)
        String seatSectionSql = "INSERT INTO seat_sections (venue_id, section_name, grade, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        String[] grades = {"NORMAL", "VIP"};  // Grade enum 값에 맞춰서 수정하세요
        for (int v = 1; v <= 100; v++) {
            for (String grade : grades) {
                jdbcTemplate.update(seatSectionSql, v, grade + "석", grade, now, now);
            }
        }
        System.out.println("SeatSection 300개 완료");

        // 3. Event 50만개 (1만개씩 batch)
        String eventSql = "INSERT INTO events (venue_id, title, description, category, event_status, open_date, end_date, deleted, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String[] categories = {"MUSICAL", "CONCERT", "PLAY", "EXHIBITION", "SPORT"};

        for (int batch = 0; batch < 50; batch++) {
            final int batchNum = batch;
            jdbcTemplate.batchUpdate(eventSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int idx = batchNum * 10000 + i;
                    ps.setLong(1, (idx % 100) + 1);
                    ps.setString(2, "콘서트_" + idx);
                    ps.setString(3, "설명_" + idx);
                    ps.setString(4, categories[idx % 5]);
                    ps.setString(5, "OPEN");
                    ps.setTimestamp(6, Timestamp.valueOf(now.plusDays(idx % 365)));
                    ps.setTimestamp(7, Timestamp.valueOf(now.plusDays(idx % 365 + 30)));
                    ps.setBoolean(8, false);
                    ps.setTimestamp(9, Timestamp.valueOf(now));
                    ps.setTimestamp(10, Timestamp.valueOf(now));
                }
                @Override
                public int getBatchSize() { return 10000; }
            });
            System.out.println("Event batch " + (batch + 1) + "/50 완료");
        }

        // 4. EventSession (Event당 1개씩 = 50만개)
        String sessionSql = "INSERT INTO event_sessions (event_id, session, session_seat_count, status, session_open_date, session_close_date, created_at, updated_at) " +
                "SELECT id, '1회차', 500, 'OPEN', open_date, end_date, ?, ? FROM events";
        jdbcTemplate.update(sessionSql, now, now);
        System.out.println("EventSession 50만개 완료");

        // 5. TicketType (Session당 1개씩 = 50만개)
        String ticketSql = "INSERT INTO ticket_types (event_session_id, seat_section_id, price, ticket_type_status, sale_open_date_time, sale_close_date_time) " +
                "SELECT es.id, " +
                "       (SELECT ss.id FROM seat_sections ss WHERE ss.venue_id = e.venue_id LIMIT 1), " +
                "       50000 + (es.id % 100000), " +
                "       'ON_SALE', " +
                "       e.open_date, " +
                "       e.end_date " +
                "FROM event_sessions es " +
                "JOIN events e ON es.event_id = e.id";
        jdbcTemplate.execute(ticketSql);
        System.out.println("TicketType 50만개 완료");

        System.out.println("=== 전체 완료! ===");
    }

    @Test
    void checkData() {
        System.out.println("Venues: " + jdbcTemplate.queryForObject("SELECT COUNT(*) FROM venues", Long.class));
        System.out.println("SeatSections: " + jdbcTemplate.queryForObject("SELECT COUNT(*) FROM seat_sections", Long.class));
        System.out.println("Events: " + jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class));
        System.out.println("Sessions: " + jdbcTemplate.queryForObject("SELECT COUNT(*) FROM event_sessions", Long.class));
        System.out.println("TicketTypes: " + jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ticket_types", Long.class));
    }
}