package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.GetEventsRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
public class EventIndexTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    Pageable pageable = PageRequest.of(0, 10);

    //JVM Warm-up 효과 때문에 발생하는 속도차이를 없애기 위해 warm up 하는 과정
    @BeforeEach
    void setUp() {
        warmUp();
        dropAllTestIndexes();
    }

    // ===================== 초 기 테 스 트 : 인덱스 없이 조회 ======================

    @Test
    @DisplayName("인덱스 없이 조회 - 10회 평균 ")
    void NoIdx_runs(){
        runPerformanceTest("no idx");
//        explainAnalyze("no idx");
    }

    // ===================== 1 차 테 스 트 : 단일 또는 복합 인덱스 ======================
    // (" {table name} / {indexed field name} ")

    @Test
    @DisplayName(" venues / location ")
    void Idx_test1(){
        createIndex("idx_venues_location", "venues", "location");
        warmUp();
        runPerformanceTest("venues / location");
//        explainAnalyze("venues / location");
    }

    // event 복합 인덱스 (where 조건 + order by)
    @Test
    @DisplayName(" events / category, event_status, open_date ")
    void Idx_test3(){
        createIndex("idx_events_category_status_opendate", "events", "category, event_status, open_date");
        warmUp();

        runPerformanceTest("events 3컬럼/ category, event_status, open_date");
//        explainAnalyze("events / category, event_status, open_date");
    }

    @Test
    @DisplayName(" events / category, event_status, open_date, end_date ")
    void Idx_test3_1(){
        createIndex("idx_events_category_status_opendate_enddate", "events", "category, event_status, open_date, end_date");
        warmUp();

        runPerformanceTest("events 4컬럼/ category, event_status, open_date, end_date");
//        explainAnalyze("events / category, event_status, open_date, end_date");
    }

    @Test
    @DisplayName(" ticket_types / event_session_id, ticket_type_status, price")
    void Idx_test4_2(){
        createIndex("idx_ticket_types_session_status_price", "ticket_types", "event_session_id, ticket_type_status, price");

        warmUp();

        runPerformanceTest("ticket_types / event_session_id, ticket_type_status, price");
//        explainAnalyze("ticket_types / event_session_id, ticket_type_status, price");
    }

    // ===================== 2 차 테 스 트 : 인덱스 여러개 조합 ======================

    @Test
    @DisplayName(" 쿼리 최적화 (events (category, event_status, open_date) + venues (location) + ticketType (session_id, status, price)) ")
    void Idx_test5(){
        createIndex("idx_events_category_status_opendate", "events", "category, event_status, open_date");
        createIndex("idx_venues_location", "venues", "location");
        createIndex("idx_ticket_types_session_status_price", "ticket_types", "event_session_id, ticket_type_status, price");

        warmUp();

        runPerformanceTest("쿼리 최적화 3조합");
//        explainAnalyze("쿼리 최적화");
    }

    @Test
    @DisplayName(" 쿼리 최적화 (events (category, event_status, open_date) + venues (location) + ticketType (session_id, status, price)) ")
    void Idx_test5_1(){
        createIndex("idx_events_category_status_opendate", "events", "category, event_status, open_date");
        createIndex("idx_venues_location", "venues", "location");
        createIndex("idx_ticket_types_session_status_price", "ticket_types", "event_session_id, ticket_type_status, price");
        createIndex("idx_events_cat_status_open_end", "events",
                "category, event_status, open_date, end_date");
        warmUp();

        runPerformanceTest("쿼리 최적화 4조합");
//        explainAnalyze("쿼리 최적화");
    }

    @Test
    @DisplayName(" 찐막 ")
    void Idx_test5_2(){
        createIndex("idx_ticket_types_session_status_price", "ticket_types", "event_session_id, ticket_type_status, price");
        createIndex("idx_events_cat_status_open_end", "events",
                "category, event_status, open_date, end_date");
        warmUp();

        runPerformanceTest("쿼리 최적화 2조합");
//        explainAnalyze("쿼리 최적화");
    }


    // ===================== 메서드들 ....... ======================

    // warm up
    private void warmUp(){
        for (int i = 0; i < 10; i++) {
            eventService.findAll(req1, pageable);
        }
    }

    // 인덱스 성능 테스트 로직
    private void runPerformanceTest(String testName) {
        GetEventsRequest[] scenarios = {
                req1, req2, req3, req4, req5
        };
        String[] scenarioNames = {
                "지역+카테고리+날짜+가격",
                "예매가능+넓은지역",
                "멀티카테고리+가격범위",
                "전국검색",
                "좁은조건"
        };

        int iterations = 10;
        long grandTotal = 0;
        int grandCount = 0;

        System.out.println("\n===== " + testName + " =====");

        for (int s = 0; s < scenarios.length; s++) {
            long scenarioTotal = 0;

            for (int i = 0; i < iterations; i++) {
                long start = System.nanoTime();
                eventService.findAll(scenarios[s], pageable);
                long elapsed = (System.nanoTime() - start) / 1_000_000;
                scenarioTotal += elapsed;
            }

            double scenarioAvg = scenarioTotal / (double) iterations;
            System.out.printf("  시나리오 %d (%s): 평균 %.1fms%n", s + 1, scenarioNames[s], scenarioAvg);
            System.out.println("\n----- 시나리오 " + (s + 1) + " EXPLAIN -----");
            List<String> result = jdbcTemplate.queryForList(explainSqls[s], String.class);
            result.forEach(System.out::println);

            grandTotal += scenarioTotal;
            grandCount += iterations;
        }

        double grandAvg = grandTotal / (double) grandCount;
        System.out.printf("  ▶ 전체 평균: %.1fms%n", grandAvg);
    }

    // index 생성 메서드
    private void createIndex(String indexName, String tableName, String columns) {
        long start = System.nanoTime();
        try {
            String sql = String.format("CREATE INDEX %s ON %s (%s)",
                    indexName, tableName, columns);
            jdbcTemplate.execute(sql);
            System.out.print("인덱스 생성: " + indexName);
            System.out.println(" | 소요 시간: "+ (System.nanoTime() - start)/ 1_000_000 +"ms");
        } catch (Exception e) {
            System.out.println("인덱스 생성 실패: " + indexName);
        }
    }

    // index 삭제 메서드
    private void dropAllTestIndexes() {
        // 1. FK 기본 인덱스 복원 (ERD 기준 모든 FK)
        Map<String, String[]> fkIndexes = Map.ofEntries(
                // event_sessions
                Map.entry("event_sessions:event_id", new String[]{"event_id"}),
                // events
                Map.entry("events:venue_id", new String[]{"venue_id"}),
                // orders
                Map.entry("orders:ticket_type_id", new String[]{"ticket_type_id"}),
                Map.entry("orders:user_id", new String[]{"user_id"}),
                // seat_sections
                Map.entry("seat_sections:venue_id", new String[]{"venue_id"}),
                // seat_sessions
                Map.entry("seat_sessions:event_session_id", new String[]{"event_session_id"}),
                Map.entry("seat_sessions:seat_id", new String[]{"seat_id"}),
                Map.entry("seat_sessions:user_id", new String[]{"user_id"}),
                // seats
                Map.entry("seats:seat_section_id", new String[]{"seat_section_id"}),
                // ticket_types
                Map.entry("ticket_types:event_session_id", new String[]{"event_session_id"}),
                Map.entry("ticket_types:seat_section_id", new String[]{"seat_section_id"})
        );

        fkIndexes.forEach((key, cols) -> {
            String table = key.split(":")[0];
            String col = key.split(":")[1];
            try {
                jdbcTemplate.execute("CREATE INDEX " + col + " ON " + table + " (" + col + ")");
            } catch (Exception ignored) {} // 이미 존재하면 무시
        });

        // 2. 테스트용 인덱스(idx_) 삭제
        String findIndexesSql = """
        SELECT DISTINCT index_name, table_name
        FROM information_schema.statistics
        WHERE index_name LIKE 'idx_%'
          AND table_schema = DATABASE()
        """;

        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(findIndexesSql);

        indexes.forEach(row -> {
            String indexName = (String) row.get("index_name");
            String tableName = (String) row.get("table_name");
            try {
                jdbcTemplate.execute("DROP INDEX " + indexName + " ON " + tableName);
                System.out.println("삭제 완료: " + indexName);
            } catch (Exception e) {
                System.out.println("삭제 실패: " + indexName + " | 원인: " + e.getMessage());
            }
        });

        // 3. temp 잔여물 정리
        try { jdbcTemplate.execute("DROP INDEX temp_fk_idx ON event_sessions"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("DROP INDEX temp_fk_idx2 ON seat_sessions"); } catch (Exception ignored) {}

    }

    // 시나리오 1: 현재 사용 중 (지역 + 카테고리 + 날짜 + 가격)
    GetEventsRequest req1 = new GetEventsRequest(
            null, List.of("SEOUL", "GYEONGGI"), List.of("MUSICAL"),
            LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0),
            null, null, 80000L
    );

    // 시나리오 2: 예매 가능한 것만 (reservePossible + 넓은 지역)
    GetEventsRequest req2 = new GetEventsRequest(
            true, List.of("BUSAN", "GYEONGNAM", "JEJU"), List.of("CONCERT"),
            LocalDateTime.of(2026, 3, 1, 0, 0), LocalDateTime.of(2026, 9, 1, 0, 0),
            "콘서트_2", null, 80000L
    );

    // 시나리오 3: 카테고리 여러 개 + 가격 범위
    GetEventsRequest req3 = new GetEventsRequest(
            null, List.of("SEOUL"), List.of("MUSICAL", "PLAY", "CONCERT"),
            LocalDateTime.of(2026, 5, 1, 0, 0), LocalDateTime.of(2026, 8, 1, 0, 0),
            null, 30000L, 60000L
    );

    // 시나리오 4: 전국 검색 (지역 많음)
    GetEventsRequest req4 = new GetEventsRequest(
            true, List.of("SEOUL", "BUSAN", "GYEONGGI", "JEJU", "GANGWON", "GYEONGNAM"),
            List.of("MUSICAL"),
            LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2027, 1, 1, 0, 0),
            null, null, 70000L
    );

    // 시나리오 5: 좁은 조건 (특정 지역 + 짧은 기간)
    GetEventsRequest req5 = new GetEventsRequest(
            null, List.of("BUSAN"), null,
            LocalDateTime.of(2026, 6, 1, 0, 0), LocalDateTime.of(2026, 7, 1, 0, 0),
            null, 10000L, 50000L
    );

    String[] explainSqls = {
            // 시나리오 1: 지역+카테고리+날짜+가격상한
            """
    EXPLAIN ANALYZE
    SELECT DISTINCT e.id, e.title, e.description, e.event_status,
           e.open_date, e.end_date, e.category, v.location, v.name
    FROM events e
    JOIN event_sessions es ON e.id = es.event_id
    JOIN ticket_types tt ON tt.event_session_id = es.id
    JOIN venues v ON v.id = e.venue_id
    WHERE v.location IN ('SEOUL', 'GYEONGGI')
      AND e.category IN ('MUSICAL')
      AND e.open_date >= '2026-03-01 00:00:00'
      AND e.end_date <= '2026-12-01 00:00:00'
      AND tt.price <= 80000
    ORDER BY e.open_date ASC
    LIMIT 10 OFFSET 10
    """,

            // 시나리오 2: 예매가능+넓은지역
            """
    EXPLAIN ANALYZE
    SELECT DISTINCT e.id, e.title, e.description, e.event_status,
                    e.open_date, e.end_date, e.category, v.location, v.name
    FROM events e
             JOIN event_sessions es ON e.id = es.event_id
             JOIN ticket_types tt ON tt.event_session_id = es.id
             JOIN venues v ON v.id = e.venue_id
    WHERE v.location IN ('BUSAN', 'GYEONGNAM', 'JEJU')
      AND e.title LIKE '콘서트_2%'
      AND e.category IN ('CONCERT')
      AND e.open_date >= '2026-03-01 00:00:00'
      AND e.end_date <= '2026-09-01 00:00:00'
      AND e.event_status != 'CLOSED'
      AND es.status != 'CLOSED'
      AND tt.ticket_type_status IN ('ON_SALE', 'PENDING')
      AND tt.price <= 80000
    ORDER BY e.open_date ASC
    LIMIT 10 OFFSET 10
    """,

            // 시나리오 3: 멀티카테고리+가격범위
            """
    EXPLAIN ANALYZE
    SELECT DISTINCT e.id, e.title, e.description, e.event_status,
           e.open_date, e.end_date, e.category, v.location, v.name
    FROM events e
    JOIN event_sessions es ON e.id = es.event_id
    JOIN ticket_types tt ON tt.event_session_id = es.id
    JOIN venues v ON v.id = e.venue_id
    WHERE v.location IN ('SEOUL')
      AND e.category IN ('MUSICAL', 'PLAY', 'CONCERT')
      AND e.open_date >= '2026-05-01 00:00:00'
      AND e.end_date <= '2026-08-01 00:00:00'
      AND tt.price BETWEEN 30000 AND 60000
    ORDER BY e.open_date ASC
    LIMIT 10 OFFSET 10
    """,

            // 시나리오 4: 전국검색
            """
    EXPLAIN ANALYZE
    SELECT DISTINCT e.id, e.title, e.description, e.event_status,
           e.open_date, e.end_date, e.category, v.location, v.name
    FROM events e
    JOIN event_sessions es ON e.id = es.event_id
    JOIN ticket_types tt ON tt.event_session_id = es.id
    JOIN venues v ON v.id = e.venue_id
    WHERE v.location IN ('SEOUL', 'BUSAN', 'GYEONGGI', 'JEJU', 'GANGWON', 'GYEONGNAM')
      AND e.category IN ('MUSICAL')
      AND e.open_date >= '2026-04-01 00:00:00'
      AND e.end_date <= '2027-01-01 00:00:00'
      AND e.event_status != 'CLOSED'
      AND es.status != 'CLOSED'
      AND tt.ticket_type_status IN ('ON_SALE', 'PENDING')
      AND tt.price <= 70000
    ORDER BY e.open_date ASC
    LIMIT 10 OFFSET 10
    """,

            // 시나리오 5: 좁은조건
            """
    EXPLAIN ANALYZE
    SELECT DISTINCT e.id, e.title, e.description, e.event_status,
           e.open_date, e.end_date, e.category, v.location, v.name
    FROM events e
    JOIN event_sessions es ON e.id = es.event_id
    JOIN ticket_types tt ON tt.event_session_id = es.id
    JOIN venues v ON v.id = e.venue_id
    WHERE v.location IN ('BUSAN')
      AND e.open_date >= '2025-06-01 00:00:00'
      AND e.end_date <= '2025-07-01 00:00:00'
      AND tt.price BETWEEN 10000 AND 50000
    ORDER BY e.open_date ASC
    LIMIT 10 OFFSET 10
    """
    };
}
