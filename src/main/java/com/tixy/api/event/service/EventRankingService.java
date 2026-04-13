package com.tixy.api.event.service;

import com.tixy.api.event.dto.response.GetRankedEventResponse;
import com.tixy.api.event.repository.EventQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRankingService {
    private final RedisTemplate<String, String> redisTemplate;
    private final EventQueryRepository eventQueryRepository;

    private static final int TOP_N = 10;
    private static final int WEEKLY_DAYS = 7;
    private static final long DAILY_TTL_SECONDS = 60 * 60 * 25;
    private static final long WEEKLY_TTL_SECONDS = 60 * 60;

    // 전체 일별 ZSet (카테고리 구분 없음)
    public static String dailyRankingKey(LocalDate date) {
        return String.format("schedule:ranking:%s", date);
    }

    // 전체 주간 집계 ZSet
    public static String weeklyRankingKey() {
        return "schedule:ranking:weekly";
    }

    // 일별 중복 방지 SET
    public static String dedupKey(Long eventId, LocalDate date) {
        return String.format("schedule:view:dedup:%d:%s", eventId, date);
    }

    public void countView(Long eventId, Long userId){
        LocalDate today = LocalDate.now();
        String dedupKey = dedupKey(eventId, today);

        log.info("[recordView] 호출됨 - scheduleId: {}, userId: {}", eventId, userId);

        Boolean isNew = redisTemplate.opsForSet().add(dedupKey, String.valueOf(userId)) == 1L;
        log.info("[recordView] SADD 결과 - dedupKey: {}, result: {}", dedupKey, isNew);

        if (!isNew) return;

        redisTemplate.expire(dedupKey, Duration.ofSeconds(DAILY_TTL_SECONDS));

        String dailyKey = dailyRankingKey(today);
        Double newScore = redisTemplate.opsForZSet().incrementScore(dailyKey, String.valueOf(eventId), 1);
        redisTemplate.expire(dailyKey, Duration.ofDays(WEEKLY_DAYS + 1));

        log.info("[recordView] ZINCRBY 결과 - dailyKey: {}, scheduleId: {}, newScore: {}", dailyKey, eventId, newScore);
    }

    public List<GetRankedEventResponse> findPopularEvents(String category) {
        String weeklyKey = weeklyRankingKey();

        // 주간 집계 캐시가 없는 경우
        if (!redisTemplate.hasKey(weeklyKey)) {
            aggregateWeekly(weeklyKey); //ZUNIONSTORE
        }

        // Redis에서 상위 조회 (카테고리 필터 고려해 넉넉하게 가져옴)
        // category 필터 있으면 상위 100개, 없으면 TOP_N개
        long fetchSize = category != null ? 100 : TOP_N;
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(weeklyKey, 0, fetchSize - 1);

        if (tuples == null || tuples.isEmpty()) return Collections.emptyList();

        Map<Long, Double> scoreMap = tuples.stream()
                .filter(t -> t.getValue() != null && t.getScore() != null)
                .collect(Collectors.toMap(
                        t -> Long.parseLong(t.getValue()),
                        ZSetOperations.TypedTuple::getScore,
                        (a, b) -> a,
                        LinkedHashMap::new  // 순서 보장
                ));

        return eventQueryRepository.fetchScheduleDetails(
                new ArrayList<>(scoreMap.keySet()), scoreMap, category);
    }


    private void aggregateWeekly(String weeklyKey) {
        LocalDate today = LocalDate.now();

        List<String> existingKeys = IntStream.range(0, WEEKLY_DAYS)
                .mapToObj(i -> dailyRankingKey(today.minusDays(i)))
                .filter(key -> Boolean.TRUE.equals(redisTemplate.hasKey(key)))
                .collect(Collectors.toList());

        if (existingKeys.isEmpty()) return;

        redisTemplate.opsForZSet().unionAndStore(
                existingKeys.get(0),
                existingKeys.subList(1, existingKeys.size()),
                weeklyKey
        );
        redisTemplate.expire(weeklyKey, Duration.ofSeconds(WEEKLY_TTL_SECONDS));
    }
}
