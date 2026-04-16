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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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


    @Autowired
    private EventQueryRepository eventQueryRepository;
    // 최고 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 안녕히가십쇼....
    // Good Bye........................ Good bye...................................................
    // virus detectec
    // 쿨.... Cool.... 음?

    @BeforeEach
    void setUp() {
        localCacheManager.getCache("eventSearch").clear();
        redisCacheManager.getCache("eventSearch").clear();
    }

    @AfterEach
    void tearDown() {
        // 데이터 정리
//        jdbcTemplate.execute("DELETE FROM events WHERE title = '강아지'");
    }

    @Test
    @DisplayName("v2 test : cache가 됨... 시간이 빨라진다")
    void cacheHitTestWithLocal() {
        GetEventsRequest request = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, "강아지", null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        // when - 같은 조건 2번 조회
        long start1 = System.currentTimeMillis();
//        List<GetEventResponse> result1 = eventService.findAllV2(request, pageable);

//        ObjectMapper om = new ObjectMapper()
//                .registerModule(new JavaTimeModule())
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                .enable(DeserializationFeature.USE_LONG_FOR_INTS);

//        List<Map<String,Object>> result1 =
//                om.convertValue(
//                        eventService.findAllV3(request, pageable),
//                        new TypeReference<List<Map<String, Object>>>() {}
//                );

        List<GetEventResponse> result3 = eventService.findAllV2(request, pageable);

        long firstCallTime = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
////        List<GetEventResponse> result2 = eventService.findAllV2(request, pageable);
//        List<Map<String,Object>> result2 =
//                om.convertValue(
//                        eventService.findAllV3(request, pageable),
//                        new TypeReference<List<Map<String, Object>>>() {}
//                );

        List<GetEventResponse> result4 = eventService.findAllV2(request, pageable);


        long secondCallTime = System.currentTimeMillis() - start2;

        System.out.println("DB 다녀옵니다: " + firstCallTime + "ms");
        System.out.println("캐시에서 가져옵니다: " + secondCallTime + "ms");

//        assertThat(result1).usingRecursiveComparison().isEqualTo(result2);

        assertThat(result3).isEqualTo(result4);
        assertThat(localCacheManager.getCache("eventSearch").get("강아지_SEOUL_0")).isNotNull();
        assertThat(secondCallTime).isLessThan(firstCallTime);
    }

    @Test
    @DisplayName("v2 test : 조건이 달라지면 cache key 도 달라짐")
    void differentConditionTestWithLocal() {
        Pageable pageable = PageRequest.of(0, 10);

        GetEventsRequest request1 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, "강아지", null, null
        );
        GetEventsRequest request2 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, "고양이", null, null
        );

        // when

        eventService.findAllV2(request1, pageable);
        eventService.findAllV2(request2, pageable);

        // then - 각각 캐시됨
        assertThat(localCacheManager.getCache("eventSearch").get("강아지_SEOUL_0")).isNotNull();
        assertThat(localCacheManager.getCache("eventSearch").get("고양이_SEOUL_0")).isNotNull();
    }


    @Test
    @DisplayName("v3 test : cache가 됨... 시간이 빨라진다")
    void cacheHitTestWithRedis() {
        GetEventsRequest request = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, "강아지", null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        // when - 같은 조건 2번 조회
        long start1 = System.currentTimeMillis();
//        List<GetEventResponse> result1 = eventService.findAllV2(request, pageable);

//        ObjectMapper om = new ObjectMapper()
//                .registerModule(new JavaTimeModule())
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                .enable(DeserializationFeature.USE_LONG_FOR_INTS);

//        List<Map<String,Object>> result1 =
//                om.convertValue(
//                        eventService.findAllV3(request, pageable),
//                        new TypeReference<List<Map<String, Object>>>() {}
//                );

        List<GetEventResponse> result3 = eventService.findAllV3(request, pageable);

        long firstCallTime = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
////        List<GetEventResponse> result2 = eventService.findAllV2(request, pageable);
//        List<Map<String,Object>> result2 =
//                om.convertValue(
//                        eventService.findAllV3(request, pageable),
//                        new TypeReference<List<Map<String, Object>>>() {}
//                );

        List<GetEventResponse> result4 = eventService.findAllV3(request, pageable);


        long secondCallTime = System.currentTimeMillis() - start2;

        System.out.println("DB 다녀옵니다: " + firstCallTime + "ms");
        System.out.println("캐시에서 가져옵니다: " + secondCallTime + "ms");

//        assertThat(result1).usingRecursiveComparison().isEqualTo(result2);

        assertThat(result3).isEqualTo(result4);
        assertThat(redisCacheManager.getCache("eventSearchRedis").get("강아지_SEOUL_0")).isNotNull();
        assertThat(secondCallTime).isLessThan(firstCallTime);
    }

    @Test
    @DisplayName("v3 test : 조건이 달라지면 cache key 도 달라짐")
    void differentConditionTestWithRedis() {
        Pageable pageable = PageRequest.of(0, 10);

        GetEventsRequest request1 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, "강아지", null, null
        );
        GetEventsRequest request2 = new GetEventsRequest(
                null, Arrays.asList("SEOUL"), null, null, null, "고양이", null, null
        );

        // when

        eventService.findAllV3(request1, pageable);
        eventService.findAllV3(request2, pageable);

        // then - 각각 캐시됨
        assertThat(redisCacheManager.getCache("eventSearchRedis").get("강아지_SEOUL_0")).isNotNull();
        assertThat(redisCacheManager.getCache("eventSearchRedis").get("고양이_SEOUL_0")).isNotNull();
    }

}