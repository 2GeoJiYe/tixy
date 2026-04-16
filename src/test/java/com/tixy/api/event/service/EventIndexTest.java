package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.GetEventsRequest;
import com.tixy.api.event.dto.response.GetEventResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@EnableCaching
@SpringBootTest
@ActiveProfiles("test")
public class EventIndexTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private CacheManager localCacheManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    Pageable pageable = PageRequest.of(0, 10);
    GetEventsRequest request = new GetEventsRequest(
            null, Arrays.asList("BUSAN"), null, null, null, null, 50000L, null
    );

    //JVM Warm-up 효과 때문에 발생하는 속도차이를 없애기 위해 warm up 하는 과정
    @BeforeEach
    void setUP() {
        for (int i = 0; i < 5; i++) {
            eventService.findAll(request, pageable);
            eventService.findAllV2(request, pageable);
            eventService.findAllV3(request, pageable);
        }
        clearAllCaches();
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 항상 인덱스 정리
        dropAllTestIndexes();
        System.out.println("===== 인덱스 정리 완료 =====\n");
    }

    /*
    ===== cache, idx 없이 실행 결과 =====
    callTime v1 (No Cache): 177ms
    callTime v2 (Local):    163ms
    callTime v3 (Redis):    175ms
    -> 모두 DB 를 갔다오기 때문에 v1, v2, v3 이 비슷한 성능을 가짐
     */
    @Test
    @DisplayName("일단... 아무런 인덱스 없이 조회 1번")
    void NoIdx(){
        // when
        long start1 = System.currentTimeMillis();
        eventService.findAll(request, pageable);
        long callTime = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        eventService.findAllV2(request, pageable);
        long callTime2 = System.currentTimeMillis() - start2;

        long start3 = System.currentTimeMillis();
        eventService.findAllV3(request, pageable);
        long callTime3 = System.currentTimeMillis() - start3;

        System.out.println("===== cache, idx 없이 실행 결과 =====");
        System.out.println("callTime v1 (No Cache): " + callTime + "ms");
        System.out.println("callTime v2 (Local):    " + callTime2 + "ms");
        System.out.println("callTime v3 (Redis):    " + callTime3 + "ms");
    }

    /*
    ===== 10회 평균 결과 =====
    callTime v1 (No Cache): 155ms
    callTime v2 (Local):    15ms
    callTime v3 (Redis):    18ms
    -> Cache 를 사용하면 2번째 요청부터는 DB 를 다녀오지 않기 때문에 속도가 빨라짐
     */
    @Test
    @DisplayName("인덱스 없이 조회 - 10회 평균 (캐싱만 있을 때 성능) ")
    void NoIdx_multiple_runs(){
        int iterations = 10;
        long totalV1 = 0, totalV2 = 0, totalV3 = 0;

        for (int i = 0; i < iterations; i++) {
            // V1 측정
            long start1 = System.nanoTime();
            eventService.findAll(request, pageable);
            totalV1 += (System.nanoTime() - start1) / 1_000_000; // ms 변환

            // V2 측정
            long start2 = System.nanoTime();
            eventService.findAllV2(request, pageable);
            totalV2 += (System.nanoTime() - start2) / 1_000_000;

            // V3 측정
            long start3 = System.nanoTime();
            eventService.findAllV3(request, pageable);
            totalV3 += (System.nanoTime() - start3) / 1_000_000;

            System.out.println("Round " + (i + 1) + " 완료");
        }

        System.out.println("\n===== " + iterations + "회 평균 결과 =====");
        System.out.println("callTime v1 (No Cache): " + totalV1 / iterations+ "ms");
        System.out.println("callTime v2 (Local):    " + totalV2 / iterations+ "ms");
        System.out.println("callTime v3 (Redis):    " + totalV3 / iterations+ "ms");
    }

    /*
    ===== event-open-date 단일 index 결과 =====
    v1 (No Cache): 157ms
    v2 (Local):    15ms
    v3 (Redis):    19ms
     */
    @Test
    @DisplayName("event-open-date 단일 index - 10회 평균 ")
    void Idx(){
        createIndex("idx_events_open_date", "events", "open_date");

        explainAnalyze("opendate");

        runPerformanceTest("event-open-date 단일 index");
    }


    @Test
    @DisplayName("status, opendate 복합 index - 10회 평균")
    void Idx_multiple_runs1(){

        createIndex("idx_events_status_opendate", "events", "event_status, open_date");

        explainAnalyze("status, opendate");

        runPerformanceTest("status, opendate 복합 index");
    }

    @Test
    @DisplayName("category, opendate, enddate 복합 index - 10회 평균")
    void Idx_multiple_runs2(){
        createIndex("idx_events_category_dates", "events", "category, open_date, end_date");

        explainAnalyze("category, opendate, enddate");

        runPerformanceTest("category, opendate, enddate 복합 index");
    }

    @Test
    @DisplayName("ticket type status, price 복합 index - 10회 평균")
    void Idx_multiple_runs3(){

        createIndex("idx_ticket_types_session_status_price", "ticket_types", "event_session_id, ticket_type_status, price");

        explainAnalyze("ticket type status, price");
        runPerformanceTest("ticket type status, price 복합 index");
    }

    @Test
    @DisplayName("venue id, location 복합 index - 10회 평균")
    void Idx_multiple_runs4(){
        createIndex("idx_venues_id_location", "venues", "id, location");

        explainAnalyze("venue id, location");
        runPerformanceTest("venue id, location 복합 index");
    }


    @Test
    @DisplayName("location, prices 복합 index - 10회 평균")
    void Idx_multiple_runs5(){
        createIndex("idx_venues_location", "venues", "location");
        createIndex("idx_ticket_types_price", "ticket_types", "price");

        explainAnalyze("location, prices");

        runPerformanceTest("location, prices 복합 index");
    }

    @Test
    @DisplayName("location, prices, dates 복합 index - 10회 평균")
    void Idx_multiple_runs6(){
        createIndex("idx_ticket_types_price", "ticket_types", "price");
        createIndex("idx_events_dates", "events", "open_date, end_date");
        createIndex("idx_venues_location", "venues", "location");

        explainAnalyze("location, prices, dates");

        runPerformanceTest("location, prices, dates 복합 index");

    }

    // 공통 성능 테스트 로직
    private void runPerformanceTest(String testName) {
        int iterations = 10;
        long totalV1 = 0, totalV2 = 0, totalV3 = 0;

        System.out.println("===== " + testName + " - " + iterations + "회 측정 =====\n");

        for (int i = 0; i < iterations; i++) {
            long start1 = System.nanoTime();
            eventService.findAll(request, pageable);
            totalV1 += (System.nanoTime() - start1) / 1_000_000;

            long start2 = System.nanoTime();
            eventService.findAllV2(request, pageable);
            totalV2 += (System.nanoTime() - start2) / 1_000_000;

            long start3 = System.nanoTime();
            eventService.findAllV3(request, pageable);
            totalV3 += (System.nanoTime() - start3) / 1_000_000;
        }

        System.out.println("===== " + testName + " 결과 =====");
        System.out.println("v1 (No Cache): " + totalV1 / iterations + "ms");
        System.out.println("v2 (Local):    " + totalV2 / iterations + "ms");
        System.out.println("v3 (Redis):    " + totalV3 / iterations + "ms");
    }

    // 캐싱된거 정리 한 번... 합니다...
    private void clearAllCaches() {
        redisCacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = redisCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        localCacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = localCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    // index 생성 메서드
    private void createIndex(String indexName, String tableName, String columns) {
        try {
            String sql = String.format("CREATE INDEX %s ON %s (%s)",
                    indexName, tableName, columns);
            jdbcTemplate.execute(sql);
            System.out.println("인덱스 생성: " + indexName);
        } catch (Exception e) {
            System.out.println("인덱스 생성 실패 (이미 존재할 수 있음): " + indexName);
        }
    }

    // index 삭제 메서드
    private void dropIndex(String indexName) {
        try {
            // MySQL/MariaDB
            jdbcTemplate.execute("DROP INDEX " + indexName + " ON event");
        } catch (Exception e) {
            // PostgreSQL (테이블명 불필요)
            try {
                jdbcTemplate.execute("DROP INDEX IF EXISTS " + indexName);
            } catch (Exception e2) {
                System.out.println("인덱스 삭제 실패: " + indexName);
            }
        }
    }

    // 모든 index 삭제 메서드
    private void dropAllTestIndexes2() {
        dropIndex("idx_location");
        dropIndex("idx_price");
        dropIndex("idx_event_date");
        dropIndex("idx_location_price");
    }

    private void dropAllTestIndexes() {
        String findIndexesSql = """
        SELECT DISTINCT index_name, table_name 
        FROM information_schema.statistics 
        WHERE table_schema = DATABASE() 
        AND index_name LIKE 'idx_%'
        AND index_name != 'PRIMARY'
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
        SELECT DISTINCT e.id, e.title, e.description, e.event_status, 
               e.open_date, e.end_date, v.location, v.name
        FROM events e
        JOIN event_sessions es ON e.id = es.event_id
        JOIN ticket_types tt ON tt.event_session_id = es.id
        JOIN venues v ON v.id = e.venue_id
        WHERE v.location IN ('BUSAN')
        AND tt.price <= 50000
        ORDER BY e.open_date ASC
        LIMIT 10 OFFSET 0
        """;

        System.out.println("\n===== " + testName + " EXPLAIN ANALYZE =====");

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        result.forEach(row -> {
            row.forEach((key, value) -> System.out.println(key + ": " + value));
            System.out.println("---");
        });
    }
}
