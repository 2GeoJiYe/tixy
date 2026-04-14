package com.tixy.api.event.service;

import com.tixy.api.event.dto.response.GetRankedEventResponse;
import com.tixy.api.event.repository.EventQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventRankingService {

    private final RedissonClient redissonClient;
    private final EventQueryRepository eventQueryRepository;

    private static final int TOP_N = 10;
    private static final int WEEKLY_DAYS = 7;
    private static final long DAILY_TTL_SECONDS = 60 * 60 * 25;
    private static final long WEEKLY_TTL_SECONDS = 60 * 60;

    public static String dailyRankingKey(LocalDate date) {
        return String.format("schedule:ranking:%s", date);
    }

    public static String weeklyRankingKey() {
        return "schedule:ranking:weekly";
    }

    public static String dedupKey(Long eventId, LocalDate date) {
        return String.format("schedule:view:dedup:%d:%s", eventId, date);
    }

    public void countView(Long eventId, Long userId) {
        LocalDate today = LocalDate.now();
        String dedupKey = dedupKey(eventId, today);

//        log.info("[countView] 호출됨 - eventId: {}, userId: {}", eventId, userId);

        RSetCache<String> dedupSet = redissonClient.getSetCache(dedupKey);
        boolean isNew = dedupSet.add(String.valueOf(userId), DAILY_TTL_SECONDS, TimeUnit.SECONDS);

//        log.info("[countView] SADD 결과 - dedupKey: {}, isNew: {}", dedupKey, isNew);
        if (!isNew) return;

        String dailyKey = dailyRankingKey(today);
        RScoredSortedSet<String> rankingSet = redissonClient.getScoredSortedSet(dailyKey);
        Double newScore = rankingSet.addScore(String.valueOf(eventId), 1);

        if (rankingSet.remainTimeToLive() == -1) {
            rankingSet.expire(Duration.ofDays(WEEKLY_DAYS + 1));
        }

//        log.info("[countView] ZINCRBY 결과 - dailyKey: {}, eventId: {}, newScore: {}", dailyKey, eventId, newScore);
    }

    public List<GetRankedEventResponse> findPopularEvents(String category) {
        String weeklyKey = weeklyRankingKey();

        RScoredSortedSet<String> weeklySet = redissonClient.getScoredSortedSet(weeklyKey);

        if (weeklySet.isEmpty()) {
            aggregateWeekly(weeklyKey);
            weeklySet = redissonClient.getScoredSortedSet(weeklyKey);
        }

        // Redis도 비어있으면 DB fallback
        if (weeklySet.isEmpty()) {
            return eventQueryRepository.findFallbackEvents(category);
        }

        long fetchSize = category != null ? 100 : TOP_N;
        Collection<ScoredEntry<String>> entries = weeklySet.entryRangeReversed(0, (int) fetchSize - 1);

        if (entries == null || entries.isEmpty()) return Collections.emptyList();

        Map<Long, Double> scoreMap = entries.stream()
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getValue()),
                        ScoredEntry::getScore,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<GetRankedEventResponse> results = eventQueryRepository.fetchScheduleDetails(
                new ArrayList<>(scoreMap.keySet()), scoreMap, category);

        // 해당 category 결과가 없으면 DB fallback
        if (results.isEmpty()) {
            return eventQueryRepository.findFallbackEvents(category);
        }

        return results;
    }

    private void aggregateWeekly(String weeklyKey) {
        LocalDate today = LocalDate.now();

        List<String> existingKeys = IntStream.range(0, WEEKLY_DAYS)
                .mapToObj(i -> dailyRankingKey(today.minusDays(i)))
                .filter(key -> !redissonClient.getScoredSortedSet(key).isEmpty())
                .toList();

        if (existingKeys.isEmpty()) return;

        RScoredSortedSet<String> weeklySet = redissonClient.getScoredSortedSet(weeklyKey);

        weeklySet.union(existingKeys.toArray(new String[0]));
        weeklySet.expire(Duration.ofSeconds(WEEKLY_TTL_SECONDS));
    }
}