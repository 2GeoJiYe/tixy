package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.GetEventsRequest;
import com.tixy.api.event.dto.response.GetEventResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@EnableCaching
@SpringBootTest
@ActiveProfiles("test")
public class EventIndexTest {

    @Autowired
    private EventService eventService;

    @Autowired
    @Qualifier("localCacheManager")
    private CacheManager localCacheManager;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager redisCacheManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    Pageable pageable = PageRequest.of(1, 10);
    GetEventsRequest request = new GetEventsRequest(
            true, List.of("BUSAN", "GYEONGNAM"), List.of("MUSICAL", "PLAY"), LocalDateTime.now(),  LocalDateTime.of(2027,1,1,0,0), "10", 30000L, 100000L
    );

    //JVM Warm-up 효과 때문에 발생하는 속도차이를 없애기 위해 warm up 하는 과정
    @BeforeEach
    void setUp() {
        dropAllTestIndexes();   // 먼저 정리
//        for (int i = 0; i < 5; i++) {  // 그 다음 warm-up
//            eventService.findAll(request, pageable);
//            eventService.findAllV2(request, pageable);
//            eventService.findAllV3(request, pageable);
//        }
//        localCacheManager.getCache("eventSearch").clear();
//        redisCacheManager.getCache("eventSearchRedis").clear();
    }

    // ===================== 초 기 테 스 트 : 인덱스 없이 조회 ======================

    @Test
    @DisplayName("인덱스 없이 조회 - 10회 평균 ")
    void NoIdx_runs(){
        runPerformanceTest("no idx");
        explainAnalyze("no idx");
    }

    // ===================== 1 차 테 스 트 : 단일 또는 복합 인덱스 ======================

    @Test
    @DisplayName(" venues / location ")
    void Idx_test1(){
        createIndex("idx_venues_location", "venues", "location");

        runPerformanceTest("venues / location");
        explainAnalyze("venues / location");
    }

    // exist subquery 최적화
    @Test
    @DisplayName(" event_sessions / event_id, status ")
    void Idx_test2(){
        createIndex("idx_event_sessions_event_id_status", "event_sessions", "event_id, status");

        runPerformanceTest(" event_sessions / event_id, status ");
        explainAnalyze(" event_sessions / event_id, status ");
    }

    // event 복합 인덱스 (where 조건 + order by)
    @Test
    @DisplayName(" events / category, event_status, open_date ")
    void Idx_test3(){
        createIndex("idx_events_category_status_opendate", "events", "category, event_status, open_date");

        runPerformanceTest("events / category, event_status, open_date");
        explainAnalyze("events / category, event_status, open_date");
    }

    @Test
    @DisplayName(" ticket_types / event_session_id, ticket_type_status, price")
    void Idx_test4(){
        createIndex("idx_ticket_types_session_status_price", "ticket_types", "event_session_id, ticket_type_status, price");

        runPerformanceTest("ticket_types / event_session_id, ticket_type_status, price");
        explainAnalyze("ticket_types / event_session_id, ticket_type_status, price");
    }

    // ===================== 2 차 테 스 트 : 인덱스 여러개 조합 ======================

    @Test
    @DisplayName(" 메인 쿼리 최적화 (1 + 3) ")
    void Idx_test5(){
        createIndex("idx_events_category_status_opendate", "events", "category, event_status, open_date");
        createIndex("idx_venues_location", "venues", "location");

        runPerformanceTest("메인 쿼리 최적화 (1 + 3)");
        explainAnalyze("메인 쿼리 최적화 (1 + 3)");
    }

    @Test
    @DisplayName(" 모든 쿼리 최적화 (1 + 2 + 3 + 4) ")
    void Idx_test6(){
        createIndex("idx_events_category_status_opendate", "events", "category, event_status, open_date");
        createIndex("idx_venues_location", "venues", "location");
        createIndex("idx_event_sessions_event_id_status", "event_sessions", "event_id, status");
        createIndex("idx_ticket_types_session_status_price", "ticket_types", "event_session_id, ticket_type_status, price");

        runPerformanceTest("모든 쿼리 최적화 (1 + 2 + 3 + 4)");
        explainAnalyze("모든 쿼리 최적화 (1 + 2 + 3 + 4)");
    }

    // ===================== 3 차 테 스 트 : 최적 인덱스 조합, 캐싱 ======================


    @Test
    @DisplayName(" 3차 테스트 with 캐싱 ")
    void Idx_test7(){
        createIndex("idx_events_category_status_opendate", "events", "category, event_status, open_date");
        createIndex("idx_venues_location", "venues", "location");

        runFinalPerformanceTest("3차 테스트 with 캐싱");
        explainAnalyze("3차 테스트 with 캐싱");
    }

    // ===================== 메서드들 ....... ======================

    // 인덱스 성능 테스트 로직
    private void runPerformanceTest(String testName) {
        int iterations = 10;
        long total = 0;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            eventService.findAll(request, pageable);  // No Cache 버전만
            total += (System.nanoTime() - start);
        }

        System.out.println("===== " + testName + " =====");
        System.out.printf("v1 (No Cache) 평균: %.3fms%n", total / (double) iterations / 1_000_000);
    }

    // 인덱스 + 캐싱 성능 테스트 로직 (최적 조합)
    private void runFinalPerformanceTest(String testName) {
        int iterations = 10;
        long totalV1 = 0, totalV2 = 0, totalV3 = 0;

        System.out.println("===== " + testName + " - " + iterations + "회 측정 =====\n");

        for (int i = 0; i < iterations; i++) {
            long start1 = System.nanoTime();
            eventService.findAll(request, pageable);
            totalV1 += (System.nanoTime() - start1) ;

            long start2 = System.nanoTime();
            eventService.findAllV2(request, pageable);
            totalV2 += (System.nanoTime() - start2);

            long start3 = System.nanoTime();
            eventService.findAllV3(request, pageable);
            totalV3 += (System.nanoTime() - start3);
        }

        System.out.println("===== " + testName + " 결과 =====");
        System.out.printf("v1 (No Cache): %.3fms%n", totalV1 / (double) iterations / 1_000_000);
        System.out.printf("v2 (Local):    %.3fms%n", totalV2 / (double) iterations / 1_000_000);
        System.out.printf("v3 (Redis):    %.3fms%n", totalV3 / (double) iterations / 1_000_000);
    }

    // index 생성 메서드
    private void createIndex(String indexName, String tableName, String columns) {
        long start = System.nanoTime();
        try {
            String sql = String.format("CREATE INDEX %s ON %s (%s)",
                    indexName, tableName, columns);
            jdbcTemplate.execute(sql);
            System.out.println("인덱스 생성: " + indexName);
            System.out.println("소요 시간: "+ (System.nanoTime() - start)/ 1_000_000);
        } catch (Exception e) {
            System.out.println("인덱스 생성 실패 (이미 존재할 수 있음): " + indexName);
        }
    }

    // index 삭제 메서드
    private void dropAllTestIndexes() {
        String findIndexesSql = """
        SELECT DISTINCT index_name, table_name
        FROM information_schema.statistics
        WHERE index_name LIKE 'idx_%'
        """;

        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(findIndexesSql);

        indexes.forEach(row -> {
            String indexName = (String) row.get("index_name");
            String tableName = (String) row.get("table_name");
            try {
                jdbcTemplate.execute("DROP INDEX " + indexName + " ON " + tableName);
                System.out.println("삭제 완료: " + indexName + " (from " + tableName + ")");
            } catch (Exception e) {
                System.out.println("삭제 실패: " + indexName);
            }
        });
    }

    // Explain analyze method
    private void explainAnalyze(String testName) {
        String sql = """
        EXPLAIN ANALYZE
        SELECT e.id, e.title, e.description, e.event_status,
               e.open_date, e.end_date, v.location, v.name
        FROM events e
        JOIN venues v ON v.id = e.venue_id
        WHERE v.location IN ('BUSAN', 'GYEONGNAM')
          AND e.open_date >= NOW()
          AND e.end_date <= '2027-01-01 00:00:00'
          AND EXISTS (
              SELECT 1
              FROM event_sessions es
              JOIN ticket_types tt ON tt.event_session_id = es.id
              WHERE es.event_id = e.id
                AND tt.price BETWEEN 30000 AND 100000
          )
        ORDER BY e.open_date ASC
        LIMIT 10 OFFSET 10
        """;

        System.out.println("\n===== " + testName + " EXPLAIN ANALYZE =====");

        List<String> result = jdbcTemplate.queryForList(sql, String.class);
        result.forEach(System.out::println);
    }
}
