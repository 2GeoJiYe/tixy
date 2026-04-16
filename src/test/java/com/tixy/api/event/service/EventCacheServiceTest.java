package com.tixy.api.event.service;

import com.tixy.api.event.dto.request.GetEventsRequest;
import com.tixy.api.event.dto.response.GetEventResponse;
import com.tixy.api.event.repository.EventQueryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableCaching
@SpringBootTest
@ActiveProfiles("test")
class EventCacheServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    @Qualifier("localCacheManager")
    private CacheManager localCacheManager;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager redisCacheManager;

    @BeforeEach
    void setUp() {
        localCacheManager.getCache("eventSearch").clear();
        redisCacheManager.getCache("eventSearchRedis").clear();
    }

    @AfterEach
    void tearDown() {
        // 데이터 정리
    }

    @Test
    @DisplayName("v2 test : cache가 됨... 시간이 빨라진다")
    void cacheHitTestWithLocal() {
        GetEventsRequest request = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, null, 50000L, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        // when - 같은 조건 2번 조회
        long start1 = System.currentTimeMillis();
        List<GetEventResponse> result1 = eventService.findAllV2(request, pageable);
        long firstCallTime = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        List<GetEventResponse> result2 = eventService.findAllV2(request, pageable);
        long secondCallTime = System.currentTimeMillis() - start2;

        System.out.println("DB 다녀옵니다: " + firstCallTime + "ms");
        System.out.println("캐시에서 가져옵니다: " + secondCallTime + "ms");

        System.out.println(result1);
        System.out.println(result2);
        // then
        assertThat(result1).isEqualTo(result2);

        // hashCode 기반 캐시 키 생성
        String cacheKey = request.hashCode() + "_" + pageable.getPageNumber();
        assertThat(localCacheManager.getCache("eventSearch").get(cacheKey)).isNotNull();
        assertThat(secondCallTime).isLessThan(firstCallTime);
    }

    @Test
    @DisplayName("v2 test : 조건이 달라지면 cache key 도 달라짐")
    void differentConditionTestWithLocal() {
        Pageable pageable = PageRequest.of(0, 10);

        GetEventsRequest request1 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, null, 50000L, null
        );
        GetEventsRequest request2 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, null, 80000L, null
        );

        // when
        eventService.findAllV2(request1, pageable);
        eventService.findAllV2(request2, pageable);

        // then - 각각 다른 캐시 키로 저장됨
        String cacheKey1 = request1.hashCode() + "_" + pageable.getPageNumber();
        String cacheKey2 = request2.hashCode() + "_" + pageable.getPageNumber();

        assertThat(localCacheManager.getCache("eventSearch").get(cacheKey1)).isNotNull();
        assertThat(localCacheManager.getCache("eventSearch").get(cacheKey2)).isNotNull();

        // 키가 달라야 함
        assertThat(cacheKey1).isNotEqualTo(cacheKey2);
    }


    @Test
    @DisplayName("v3 test : cache가 됨... 시간이 빨라진다")
    void cacheHitTestWithRedis() {
        GetEventsRequest request = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, null, 50000L, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        // when - 같은 조건 2번 조회
        long start1 = System.currentTimeMillis();
        List<GetEventResponse> result1 = eventService.findAllV3(request, pageable);
        long firstCallTime = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        List<GetEventResponse> result2 = eventService.findAllV3(request, pageable);
        long secondCallTime = System.currentTimeMillis() - start2;

        System.out.println("DB 다녀옵니다: " + firstCallTime + "ms");
        System.out.println("캐시에서 가져옵니다: " + secondCallTime + "ms");

        // then
        assertThat(result1).isEqualTo(result2);

        // hashCode 기반 캐시 키 생성
        String cacheKey = request.hashCode() + "_" + pageable.getPageNumber();
        assertThat(redisCacheManager.getCache("eventSearchRedis").get(cacheKey)).isNotNull();
        assertThat(secondCallTime).isLessThan(firstCallTime);
    }

    @Test
    @DisplayName("v3 test : 조건이 달라지면 cache key 도 달라짐")
    void differentConditionTestWithRedis() {
        Pageable pageable = PageRequest.of(0, 10);

        GetEventsRequest request1 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, null, 50000L, null
        );
        GetEventsRequest request2 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, null, 80000L, null
        );

        // when
        eventService.findAllV3(request1, pageable);
        eventService.findAllV3(request2, pageable);

        // then - 각각 다른 캐시 키로 저장됨
        String cacheKey1 = request1.hashCode() + "_" + pageable.getPageNumber();
        String cacheKey2 = request2.hashCode() + "_" + pageable.getPageNumber();

        assertThat(redisCacheManager.getCache("eventSearchRedis").get(cacheKey1)).isNotNull();
        assertThat(redisCacheManager.getCache("eventSearchRedis").get(cacheKey2)).isNotNull();

        // 키가 달라야 함
        assertThat(cacheKey1).isNotEqualTo(cacheKey2);
    }
}