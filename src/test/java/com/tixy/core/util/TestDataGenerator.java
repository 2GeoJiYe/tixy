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
//@Disabled  // 실행할 때는 주석 처리!
class TestDataGenerator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ─────────────────────────────────────────────
    // 지역별 venue 분배 (서울 집중)
    // 총 100개 venue
    //   SEOUL: 35개 / GYEONGGI: 20개 / BUSAN: 15개
    //   GYEONGNAM: 8개 / GANGWON: 7개 / CHUNGNAM: 5개
    //   CHUNGBUK: 3개 / JEONBUK: 3개 / JEONNAM: 2개
    //   JEJU: 1개 / GYEONGBUK: 1개
    // ─────────────────────────────────────────────
    private static final String[] VENUE_LOCATIONS = buildVenueLocations();

    private static String[] buildVenueLocations() {
        String[] locations = new String[100];
        int idx = 0;

        for (int i = 0; i < 35; i++) locations[idx++] = "SEOUL";
        for (int i = 0; i < 20; i++) locations[idx++] = "GYEONGGI";
        for (int i = 0; i < 15; i++) locations[idx++] = "BUSAN";
        for (int i = 0; i < 8; i++)  locations[idx++] = "GYEONGNAM";
        for (int i = 0; i < 7; i++)  locations[idx++] = "GANGWON";
        for (int i = 0; i < 5; i++)  locations[idx++] = "CHUNGNAM";
        for (int i = 0; i < 3; i++)  locations[idx++] = "CHUNGBUK";
        for (int i = 0; i < 3; i++)  locations[idx++] = "JEONBUK";
        for (int i = 0; i < 2; i++)  locations[idx++] = "JEONNAM";
        for (int i = 0; i < 1; i++)  locations[idx++] = "JEJU";
        for (int i = 0; i < 1; i++)  locations[idx++] = "GYEONGBUK";

        return locations;
    }

    @Test
    void generateTestData() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Venue 100개 (서울 집중 분배)
        Boolean venuesExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM venues LIMIT 1)", Boolean.class);
        if (venuesExist == null || !venuesExist) {
            System.out.println("=== Step 1: Venue 생성 ===");
            String venueSql = "INSERT INTO venues (name, venue_status, location, total_seat_count, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";

            for (int v = 0; v < 100; v++) {
                jdbcTemplate.update(venueSql,
                        VENUE_LOCATIONS[v] + "_Venue_" + (v + 1),
                        "ACTIVE",
                        VENUE_LOCATIONS[v],
                        1000 + v * 10,
                        now, now
                );
            }
            System.out.println("✅ Venue 100개 완료");
            printVenueDistribution();
        } else {
            System.out.println("⏭️  Venue 이미 존재 - 건너뜀");
        }

        // 2. SeatSection (Venue당 3개씩 = 300개)
        Boolean seatSectionsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM seat_sections LIMIT 1)", Boolean.class);
        if (seatSectionsExist == null || !seatSectionsExist) {
            System.out.println("=== Step 2: SeatSection 생성 ===");
            String seatSectionSql = "INSERT INTO seat_sections (venue_id, section_name, grade, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            String[] grades = {"NORMAL", "VIP", "AGRADE"};

            for (int v = 1; v <= 100; v++) {
                for (int g = 0; g < grades.length; g++) {
                    jdbcTemplate.update(seatSectionSql,
                            v, grades[g] + "석_" + (g + 1), grades[g], now, now);
                }
            }
            System.out.println("✅ SeatSection 300개 완료");
        } else {
            System.out.println("⏭️  SeatSection 이미 존재 - 건너뜀");
        }

        // 3. Event 5만개 (1만개씩 batch)
        Boolean eventsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM events LIMIT 1)", Boolean.class);
        if (eventsExist == null || !eventsExist) {
            System.out.println("=== Step 3: Event 생성 (5만개) ===");

            String eventSql = "INSERT INTO events (venue_id, title, description, category, event_status, open_date, end_date, deleted, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String[] categories = {"MUSICAL", "CONCERT", "PLAY", "EXHIBITION", "SPORT"};

            for (int batch = 0; batch < 5; batch++) {
                final int batchNum = batch;
                jdbcTemplate.batchUpdate(eventSql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int idx = batchNum * 10000 + i;
                        // venue_id 1~100 순환 → 서울 venue가 35개라 서울 이벤트가 자연스럽게 35% 차지
                        int venueId = (idx % 100) + 1;

                        // 날짜: 과거 70% / 미래 30%
                        int offsetDays;
                        if (idx % 10 < 7) {
                            offsetDays = -(ThreadLocalRandom.current().nextInt(1, 1501));
                        } else {
                            offsetDays = ThreadLocalRandom.current().nextInt(1, 731);
                        }
                        LocalDateTime openDate = now.plusDays(offsetDays);
                        LocalDateTime endDate = openDate.plusDays(ThreadLocalRandom.current().nextInt(1, 101));

                        // 상태 결정
                        String status;
                        if (now.isBefore(openDate)) {
                            status = "SCHEDULED";
                        } else if (now.isAfter(endDate)) {
                            status = "CLOSED";
                        } else {
                            status = "OPEN";
                        }

                        ps.setLong(1, venueId);
                        ps.setString(2, "이벤트_" + idx);
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
                System.out.println("Event batch " + (batch + 1) + "/5 완료");
            }
            System.out.println("✅ Event 5만개 완료");
        } else {
            System.out.println("⏭️  Event 이미 존재 - 건너뜀");
        }

        // 4. EventSession (Event당 1개씩 = 5만개)
        Boolean sessionsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM event_sessions LIMIT 1)", Boolean.class);
        if (sessionsExist == null || !sessionsExist) {
            System.out.println("=== Step 4: EventSession 생성 ===");

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

        // 5. TicketType (Session당 3개씩 = 15만개)
        Boolean ticketTypesExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM ticket_types LIMIT 1)", Boolean.class);
        if (ticketTypesExist == null || !ticketTypesExist) {
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
                WHERE ss.grade IN ('NORMAL', 'AGRADE', 'VIP')
                LIMIT 150000
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

        // 지역별 이벤트 분포
        printEventDistribution();

        // 관계 검증
        System.out.println("\n=== 관계 검증 ===");

        Long orphanEvents = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events e LEFT JOIN venues v ON e.venue_id = v.id WHERE v.id IS NULL", Long.class);
        System.out.println("Venue 없는 Event: " + orphanEvents + (orphanEvents == 0 ? " ✅" : " ❌"));

        Long orphanSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM event_sessions es LEFT JOIN events e ON es.event_id = e.id WHERE e.id IS NULL", Long.class);
        System.out.println("Event 없는 Session: " + orphanSessions + (orphanSessions == 0 ? " ✅" : " ❌"));

        Long orphanTickets = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket_types tt LEFT JOIN event_sessions es ON tt.event_session_id = es.id WHERE es.id IS NULL", Long.class);
        System.out.println("Session 없는 TicketType: " + orphanTickets + (orphanTickets == 0 ? " ✅" : " ❌"));

        Long orphanTicketSeats = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket_types tt LEFT JOIN seat_sections ss ON tt.seat_section_id = ss.id WHERE ss.id IS NULL", Long.class);
        System.out.println("SeatSection 없는 TicketType: " + orphanTicketSeats + (orphanTicketSeats == 0 ? " ✅" : " ❌"));

        // JOIN 테스트
        System.out.println("\n=== JOIN 테스트 ===");
        Long joinCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events e " +
                        "JOIN event_sessions es ON e.id = es.event_id " +
                        "JOIN ticket_types tt ON tt.event_session_id = es.id " +
                        "JOIN venues v ON v.id = e.venue_id", Long.class);
        System.out.println("JOIN 성공 건수: " + String.format("%,d", joinCount));

        // 샘플 데이터
        System.out.println("\n=== 샘플 데이터 (첫 5개) ===");
        List<Map<String, Object>> samples = jdbcTemplate.queryForList(
                "SELECT e.id AS event_id, e.title, v.name AS venue_name, v.location, " +
                        "es.session, tt.price, ss.section_name " +
                        "FROM events e " +
                        "JOIN event_sessions es ON e.id = es.event_id " +
                        "JOIN ticket_types tt ON tt.event_session_id = es.id " +
                        "JOIN venues v ON v.id = e.venue_id " +
                        "JOIN seat_sections ss ON ss.id = tt.seat_section_id " +
                        "LIMIT 5");

        samples.forEach(row -> System.out.println(String.format(
                "Event %s | %s | %s (%s) | %s | %,d원 | %s",
                row.get("event_id"), row.get("title"), row.get("venue_name"),
                row.get("location"), row.get("session"), row.get("price"),
                row.get("section_name")
        )));
    }

    private void printVenueDistribution() {
        System.out.println("\n=== Venue 지역별 분포 ===");
        List<Map<String, Object>> dist = jdbcTemplate.queryForList(
                "SELECT location, COUNT(*) as cnt FROM venues GROUP BY location ORDER BY cnt DESC");
        dist.forEach(row -> System.out.println(String.format(
                "  %s: %s개", row.get("location"), row.get("cnt"))));
    }

    private void printEventDistribution() {
        System.out.println("\n=== 지역별 이벤트 분포 ===");
        List<Map<String, Object>> dist = jdbcTemplate.queryForList(
                "SELECT v.location, COUNT(*) as cnt FROM events e " +
                        "JOIN venues v ON e.venue_id = v.id " +
                        "GROUP BY v.location ORDER BY cnt DESC");
        dist.forEach(row -> System.out.println(String.format(
                "  %s: %,d건", row.get("location"), row.get("cnt"))));

        System.out.println("\n=== 카테고리별 이벤트 분포 ===");
        List<Map<String, Object>> catDist = jdbcTemplate.queryForList(
                "SELECT category, COUNT(*) as cnt FROM events GROUP BY category ORDER BY cnt DESC");
        catDist.forEach(row -> System.out.println(String.format(
                "  %s: %,d건", row.get("category"), row.get("cnt"))));

        System.out.println("\n=== 상태별 이벤트 분포 ===");
        List<Map<String, Object>> statusDist = jdbcTemplate.queryForList(
                "SELECT event_status, COUNT(*) as cnt FROM events GROUP BY event_status ORDER BY cnt DESC");
        statusDist.forEach(row -> System.out.println(String.format(
                "  %s: %,d건", row.get("event_status"), row.get("cnt"))));
    }

    @Test
    void cleanUpData() {
        System.out.println("=== 데이터 삭제 시작 ===");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.execute("TRUNCATE TABLE ticket_types");
        System.out.println("TicketTypes 삭제");

        jdbcTemplate.execute("TRUNCATE TABLE event_sessions");
        System.out.println("EventSessions 삭제");

        jdbcTemplate.execute("TRUNCATE TABLE events");
        System.out.println("Events 삭제");

        jdbcTemplate.execute("TRUNCATE TABLE seat_sections");
        System.out.println("SeatSections 삭제");

        jdbcTemplate.execute("TRUNCATE TABLE venues");
        System.out.println("Venues 삭제");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        System.out.println("\n=== 삭제 완료! ===");
        checkData();
    }
}
