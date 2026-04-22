package com.tixy.api.event.service;

import com.tixy.api.event.dto.response.GetRankedEventResponse;
import com.tixy.api.event.repository.EventQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final long WEEKLY_TTL_SECONDS = 60 * 60 * 24;

    public static String dailyRankingKey(LocalDate date) {
        return String.format("event:ranking:daily:%s", date);
    }

    public static String weeklyRankingKey() {
        return "event:ranking:weekly";
    }

    private String dedupKey(Long eventId) {
        // dedupKey는 TTL 기반이고 dailyKey는 LocalDate 기반이라서
        // dedup key 는 살아있는데 Daily key 는 바뀌어서 같은 날 같은 사람의 조회수가 이중 카운트 되지 않도록 함
        return "event:view:dedup:" + eventId + ":" + LocalDate.now();
    }


    public void countView(Long eventId, Long userId) {
        String dedupKey = dedupKey(eventId);
        LocalDate today = LocalDate.now();

        log.info("[countView] 호출됨 - eventId: {}, userId: {}", eventId, userId);

        RSetCache<String> dedupSet = redissonClient.getSetCache(dedupKey);
        long secondsUntilMidnight = Duration.between(
                LocalDateTime.now(),
                today.plusDays(1).atStartOfDay()
        ).getSeconds();

        boolean isNew = dedupSet.add(String.valueOf(userId), secondsUntilMidnight, TimeUnit.SECONDS);

        if (!isNew) return;

        // dedupSet 자체의 TTL도 자정까지로 설정
        // 원래 24시간 TTL 이었는데 그냥 자정 기준으로 할 수 있도록 함
        dedupSet.expireIfNotSet(Duration.ofSeconds(secondsUntilMidnight));

        String dailyKey = dailyRankingKey(today);
        RScoredSortedSet<String> rankingSet = redissonClient.getScoredSortedSet(dailyKey);
        Double newScore = rankingSet.addScore(String.valueOf(eventId), 1);

        if (rankingSet.remainTimeToLive() == -1) {
            rankingSet.expire(Duration.ofDays(WEEKLY_DAYS + 1));
        }

//        log.info("count view 결과 - eventId: {}, userId: {}, newScore: {}", eventId, userId, newScore);
    }

    public List<GetRankedEventResponse> findPopularEvents(String category) throws InterruptedException {
        String weeklyKey = weeklyRankingKey();

        RScoredSortedSet<String> weeklySet = redissonClient.getScoredSortedSet(weeklyKey);

        // weekly set 요청 동시성 방지
        if (weeklySet.isEmpty()) {
            RLock lock = redissonClient.getLock("lock:weekly-aggregate");
            if (lock.tryLock(0, 10, TimeUnit.SECONDS)) {
                try {
                    // 락 획득 후 다시 확인 (double-check)
                    weeklySet = redissonClient.getScoredSortedSet(weeklyKey);
                    if (weeklySet.isEmpty()) {
                        aggregateWeekly();
                    }
                } finally {
                    lock.unlock();
                }
            }
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

    public void aggregateWeekly() {
        String weeklyKey = weeklyRankingKey();
        String tempKey = weeklyKey + ":temp";
        LocalDate today = LocalDate.now();

        String[] dailyKeys = IntStream.range(0, WEEKLY_DAYS)
                .mapToObj(i -> dailyRankingKey(today.minusDays(i)))
                .toArray(String[]::new);

        RScoredSortedSet<String> tempSet = redissonClient.getScoredSortedSet(tempKey);
        tempSet.delete();
        tempSet.union(dailyKeys);

        if (tempSet.isEmpty()) {
            tempSet.delete();
            return;
        }

        tempSet.expire(Duration.ofSeconds(WEEKLY_TTL_SECONDS));
        tempSet.rename(weeklyKey);
    }

    public void evictViewCache(Long eventId) {
        String dedupKey = dedupKey(eventId);
        RSetCache<String> dedupSet = redissonClient.getSetCache(dedupKey);
        boolean deleted = dedupSet.delete();

        log.info("[evictViewCache] eventId: {}, deleted: {}", eventId, deleted);
    }
}