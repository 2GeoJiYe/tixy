package com.tixy.core.util;

import org.junit.jupiter.api.Disabled;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
@ActiveProfiles("test")
@Disabled  // 실행할 때는 주석 처리!
class TestDataGenerator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void generateTestData() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Venue 100개
        Boolean venuesExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM venues LIMIT 1)",
                Boolean.class
        );
        if (venuesExist== null || !venuesExist) {
            System.out.println("=== Step 1: Venue 생성 ===");
            String venueSql = "INSERT INTO venues (name, venue_status, location, total_seat_count, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            String[] locations = {"SEOUL", "BUSAN", "GYEONGGI", "JEJU", "GYEONGNAM", "GANGWON", "CHUNGBUK", "CHUNGNAM", "JEONBUK", "JEONNAM", "GYEONGBUK"};

            for (int v = 1; v <= 100; v++) {
                jdbcTemplate.update(venueSql,
                        "Venue_" + v,
                        "ACTIVE",
                        locations[v % 11],
                        1000 + v * 10,
                        now,
                        now
                );
            }
            System.out.println("✅ Venue 100개 완료");
        } else {
            System.out.println("⏭️  Venue 이미 존재 - 건너뜀");
        }

        // 2. SeatSection (Venue당 3개씩 = 300개)
        Boolean seatSectionsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM seat_sections LIMIT 1)",
                Boolean.class
        );
        if (seatSectionsExist== null || !seatSectionsExist) {
            System.out.println("=== Step 2: SeatSection 생성 ===");
            String seatSectionSql = "INSERT INTO seat_sections (venue_id, section_name, grade, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            String[] grades = {"NORMAL", "VIP", "AGRADE"};

            for (int v = 1; v <= 100; v++) {
                for (int g = 0; g < grades.length; g++) {
                    jdbcTemplate.update(seatSectionSql,
                            v,
                            grades[g] + "석_" + (g + 1),
                            grades[g],
                            now,
                            now
                    );
                }
            }
            System.out.println("✅ SeatSection 300개 완료");
        } else {
            System.out.println("⏭️  SeatSection 이미 존재 - 건너뜀");
        }

        // 3. Event 50만개 (1만개씩 batch)
        Boolean eventsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM events LIMIT 1)",
                Boolean.class
        );
        if (eventsExist== null || !eventsExist) {
            System.out.println("=== Step 3: Event 생성 (20만개) ===");

            String eventSql = "INSERT INTO events (venue_id, title, description, category, event_status, open_date, end_date, deleted, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String[] categories = {"MUSICAL", "CONCERT", "PLAY", "EXHIBITION", "SPORT"};

            for (int batch = 0; batch < 50; batch++) {
                final int batchNum = batch;
                jdbcTemplate.batchUpdate(eventSql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int idx = batchNum * 10000 + i;
                        int venueId = (idx % 100) + 1;

                        // 날짜 생성: -200일 ~ +365일 범위로 분산
                        int offsetDays;
                        if (idx % 10 < 7) {
                            // 과거: -730 ~ -1
                            offsetDays = -(ThreadLocalRandom.current().nextInt(1, 1501));
                        } else {
                            // 미래: +1 ~ +365
                            offsetDays = ThreadLocalRandom.current().nextInt(1, 731);
                        }
                        LocalDateTime openDate = now.plusDays(offsetDays);
                        LocalDateTime endDate = openDate.plusDays(venueId); // 1~100 일

                        // 날짜 기준 상태 결정
                        String status;
                        if (now.isBefore(openDate)) {
                            status = "SCHEDULED";  // now < open < end
                        } else if (now.isAfter(endDate)) {
                            status = "CLOSED";     // open < end < now
                        } else {
                            status = "OPEN";       // open < now < end
                        }

                        ps.setLong(1, venueId);
                        ps.setString(2, "콘서트_" + idx);
                        ps.setString(3, "설명_" + idx);
                        ps.setString(4, categories[idx % 5]);
                        ps.setString(5, status);
                        ps.setTimestamp(6, Timestamp.valueOf(openDate));
                        ps.setTimestamp(7, Timestamp.valueOf(endDate));
                        ps.setBoolean(8, false);
                        ps.setTimestamp(9, Timestamp.valueOf(now));
                        ps.setTimestamp(10, Timestamp.valueOf(now));
                    }
                    @Override
                    public int getBatchSize() { return 10000; }
                });
                System.out.println("Event batch " + (batch + 1) + "/50 완료");
            }
            System.out.println("✅ Event 50만개 완료");
        } else {
            System.out.println("⏭️  Event 이미 존재 - 건너뜀");
        }

        // 4. EventSession (Event당 1개씩 = 50만개)
        Boolean sessionsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM event_sessions LIMIT 1)",
                Boolean.class
        );
        if (sessionsExist== null || !sessionsExist) {
            System.out.println("=== Step 4: EventSession 생성  ===");

            String sessionSql = """
                INSERT INTO event_sessions (event_id, session, session_seat_count, status, session_open_date, session_close_date, created_at, updated_at)
                SELECT e.id, '1회차', 500,
                    CASE e.event_status
                        WHEN 'SCHEDULED' THEN 'SCHEDULED'
                        WHEN 'OPEN' THEN 'ON_PERFORM'
                        WHEN 'CLOSED' THEN 'CLOSED'
                    END,
                    e.open_date, e.end_date, ?, ?
                FROM events e
                WHERE e.deleted = false
                """;

            jdbcTemplate.update(sessionSql, now, now);
            System.out.println("✅ EventSession 생성 완료");
        } else {
            System.out.println("⏭️  EventSession 이미 존재 - 건너뜀");
        }

        // 5. TicketType (Session당 평균 3개씩 = 100만개)
        Boolean ticketTypesExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM ticket_types LIMIT 1)",
                Boolean.class
        );
        if (ticketTypesExist== null || !ticketTypesExist) {
            System.out.println("=== Step 5: TicketType 생성 ===");

            String ticketSql = """
    INSERT INTO ticket_types
    (event_session_id, seat_section_id, price, ticket_type_status, sale_open_date_time, sale_close_date_time)
    SELECT
        es.id AS event_session_id,
        ss.id AS seat_section_id,
        CASE
            WHEN ss.grade = 'VIP' THEN 100000 + (es.id * 7 + ss.id * 13) % 100001
            ELSE 30000 + (es.id * 7 + ss.id * 13) % 100001
        END AS price,
        CASE
            WHEN e.event_status = 'CLOSED' THEN 'SALE_ENDED'
            WHEN e.event_status = 'SCHEDULED' THEN
                CASE (es.id + ss.id) % 3
                    WHEN 0 THEN 'PENDING'
                    WHEN 1 THEN 'ON_SALE'
                    ELSE 'ON_SALE'
                END
            WHEN e.event_status = 'OPEN' THEN
                CASE (es.id + ss.id) % 2
                    WHEN 0 THEN 'ON_SALE'
                    ELSE 'SOLD_OUT'
                END
            ELSE 'ON_SALE'
        END AS ticket_type_status,
        e.open_date AS sale_open_date_time,
        e.end_date AS sale_close_date_time
    FROM event_sessions es
    JOIN events e ON es.event_id = e.id
    JOIN seat_sections ss ON ss.venue_id = e.venue_id
    WHERE ss.grade IN ('NORMAL', 'AGRADE','VIP')
    LIMIT 1500000
    """;

            jdbcTemplate.execute(ticketSql);
            System.out.println("✅ TicketType 생성 완료");
        } else {
            System.out.println("⏭️  TicketType 이미 존재 - 건너뜀");
        }

        System.out.println("\n=== 전체 완료! ===");
        checkData();
    }

    @Test
    void checkData() {
        System.out.println("\n=== 데이터 검증 ===");

        Long venueCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM venues", Long.class);
        Long seatSectionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM seat_sections", Long.class);
        Long eventCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);
        Long sessionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM event_sessions", Long.class);
        Long ticketTypeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ticket_types", Long.class);

        System.out.println("Venues:       " + String.format("%,d", venueCount));
        System.out.println("SeatSections: " + String.format("%,d", seatSectionCount));
        System.out.println("Events:       " + String.format("%,d", eventCount));
        System.out.println("Sessions:     " + String.format("%,d", sessionCount));
        System.out.println("TicketTypes:  " + String.format("%,d", ticketTypeCount));

        // 관계 검증
        System.out.println("\n=== 관계 검증 ===");

        // Event - Venue 관계
        Long orphanEvents = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events e LEFT JOIN venues v ON e.venue_id = v.id WHERE v.id IS NULL",
                Long.class
        );
        System.out.println("Venue 없는 Event: " + orphanEvents + (orphanEvents == 0 ? " true " : " false "));

        // EventSession - Event 관계
        Long orphanSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM event_sessions es LEFT JOIN events e ON es.event_id = e.id WHERE e.id IS NULL",
                Long.class
        );
        System.out.println("Event 없는 Session: " + orphanSessions + (orphanSessions == 0 ? " true " : " false "));

        // TicketType - EventSession 관계
        Long orphanTickets = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket_types tt LEFT JOIN event_sessions es ON tt.event_session_id = es.id WHERE es.id IS NULL",
                Long.class
        );
        System.out.println("Session 없는 TicketType: " + orphanTickets + (orphanTickets == 0 ? " true " : " false "));

        // TicketType - SeatSection 관계
        Long orphanTicketSeats = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket_types tt LEFT JOIN seat_sections ss ON tt.seat_section_id = ss.id WHERE ss.id IS NULL",
                Long.class
        );
        System.out.println("SeatSection 없는 TicketType: " + orphanTicketSeats + (orphanTicketSeats == 0 ? " true " : " false "));

        // JOIN 테스트
        System.out.println("\n=== JOIN 테스트 ===");
        Long joinCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM events e " +
                        "JOIN event_sessions es ON e.id = es.event_id " +
                        "JOIN ticket_types tt ON tt.event_session_id = es.id " +
                        "JOIN venues v ON v.id = e.venue_id",
                Long.class
        );
        System.out.println("JOIN 성공 건수: " + String.format("%,d", joinCount));

        // 샘플 데이터 조회
        System.out.println("\n=== 샘플 데이터 (첫 5개) ===");
        List<Map<String, Object>> samples = jdbcTemplate.queryForList(
                "SELECT " +
                        "    e.id AS event_id, " +
                        "    e.title, " +
                        "    v.name AS venue_name, " +
                        "    es.session, " +
                        "    tt.price, " +
                        "    ss.section_name " +
                        "FROM events e " +
                        "JOIN event_sessions es ON e.id = es.event_id " +
                        "JOIN ticket_types tt ON tt.event_session_id = es.id " +
                        "JOIN venues v ON v.id = e.venue_id " +
                        "JOIN seat_sections ss ON ss.id = tt.seat_section_id " +
                        "LIMIT 5"
        );

        samples.forEach(row -> {
            System.out.println(String.format(
                    "Event %s | %s | %s | %s | %,d원 | %s",
                    row.get("event_id"),
                    row.get("title"),
                    row.get("venue_name"),
                    row.get("session"),
                    row.get("price"),
                    row.get("section_name")
            ));
        });
    }

    @Test
    void cleanUpData() {
        System.out.println("=== 데이터 삭제 시작 ===");

        jdbcTemplate.execute("DELETE FROM ticket_types");
        System.out.println("TicketTypes 삭제");

        jdbcTemplate.execute("DELETE FROM event_sessions");
        System.out.println("EventSessions 삭제");

        jdbcTemplate.execute("DELETE FROM events");
        System.out.println("Events 삭제");

        jdbcTemplate.execute("DELETE FROM seat_sections");
        System.out.println("SeatSections 삭제");

        jdbcTemplate.execute("DELETE FROM venues");
        System.out.println("Venues 삭제");

        System.out.println("\n=== 삭제 완료! ===");
        checkData();
    }
}