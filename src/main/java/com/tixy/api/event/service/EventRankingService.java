package com.tixy.api.event.service;

import com.tixy.api.event.dto.response.GetRankedEventResponse;
import com.tixy.api.event.repository.EventQueryRepository;
import com.tixy.api.venue.enums.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.scheduling.annotation.Scheduled;
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

        // RSetCache 대신 RSet 사용: 멤버별 TTL이 불필요하므로
        // score에 만료시각을 저장하는 오버헤드를 제거하고, 키 단위 TTL로 만료 관리
        RSet<String> dedupSet = redissonClient.getSet(dedupKey);

        boolean isNew = dedupSet.add(String.valueOf(userId));

        if (!isNew) return;

        // set 자체의 TTL 만 설정 -> 불필요한 값을 저장하지 않음, 멤버가 추가되어도 기존 만료 시점 유지
        dedupSet.expireIfNotSet(Duration.ofDays(8));

        // 날짜 기반 키 생성 eventId - count 로 저장
        String dailyKey = dailyRankingKey(today);
        RScoredSortedSet<String> rankingSet = redissonClient.getScoredSortedSet(dailyKey);

        // 새로운 사용자가 조회할 때마다 +1씩 쌓임
        rankingSet.addScore(String.valueOf(eventId), 1);
        rankingSet.expireIfNotSet(Duration.ofDays(WEEKLY_DAYS + 1));

//        log.info("count view 결과 - eventId: {}, userId: {}, newScore: {}", eventId, userId, newScore);
    }

    public List<GetRankedEventResponse> findPopularEvents(String category) {
        String weeklyKey = weeklyRankingKey();

        RScoredSortedSet<String> weeklySet = redissonClient.getScoredSortedSet(weeklyKey);

        // weekly set 요청 동시성 방지
        if (weeklySet.isEmpty()) {
            RLock lock = redissonClient.getLock("lock:weekly-aggregate");
            boolean locked = false;
            try {
                locked = lock.tryLock(0, 10, TimeUnit.SECONDS);
                if (locked) {
                    weeklySet = redissonClient.getScoredSortedSet(weeklyKey);
                    if (weeklySet.isEmpty()) {
                        aggregateWeekly();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // interrupt 상태 복원
                throw new RuntimeException("Weekly aggregation interrupted", e);
            } finally {
                if (locked) {
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

        // 유효하지 않은 카테고리가 들어오면 빈 리스트만 내보내는게 아니라 예외처리 해주기
        if (category!=null){
            Category.from(category);
        }

        List<GetRankedEventResponse> results = eventQueryRepository.fetchScheduleDetails(
                new ArrayList<>(scoreMap.keySet()), scoreMap, category);

        // 해당 category 결과가 없으면 DB fallback
        if (results.isEmpty()) {
            return eventQueryRepository.findFallbackEvents(category);
        }

        return results;
    }

    public void aggregateWeekly() {
        RLock lock = redissonClient.getLock("lock:weekly-ranking-aggregate");
        if (!lock.tryLock()) {
            // 이미 다른 인스턴스가 실행 중이면 스킵
            return;
        }

        try{
            String weeklyKey = weeklyRankingKey();
            String tempKey = weeklyKey + ":temp";
            LocalDate today = LocalDate.now();

            // daily 키가 없으면 dedup에서 복구 시도
            for (int i = 0; i < WEEKLY_DAYS; i++) {
                rebuildDailyRanking(today.minusDays(i));
            }

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
        } finally {
            lock.unlock();
        }
    }

    public void evictViewCache(Long eventId) {
        String dedupKey = dedupKey(eventId);
        RSetCache<String> dedupSet = redissonClient.getSetCache(dedupKey);
        boolean deleted = dedupSet.delete();

        log.info("[evictViewCache] eventId: {}, deleted: {}", eventId, deleted);
    }

    // daily ranking key 삭제 했을 때 복구시키는 메서드
    public void rebuildDailyRanking(LocalDate date) {
        String dailyKey = dailyRankingKey(date);
        RScoredSortedSet<String> rankingSet = redissonClient.getScoredSortedSet(dailyKey);

        if (!rankingSet.isEmpty()) {
            log.info("[rebuildDailyRanking] 이미 존재함 - key: {}", dailyKey);
            return;
        }

        // dedup 키들에서 역집계
        String pattern = "event:view:dedup:*:" + date;
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);

        for (String dedupKey : keys) {
            // "event:view:dedup:5:2026-04-22" → eventId = 5
            String[] parts = dedupKey.split(":");
            String eventId = parts[3];

            long viewCount = redissonClient.getSetCache(dedupKey).size();
            if (viewCount > 0) {
                rankingSet.addScore(eventId, viewCount);
            }
        }

        rankingSet.expire(Duration.ofDays(WEEKLY_DAYS + 1));
        log.info("[rebuildDailyRanking] 복구 완료 - key: {}, entries: {}", dailyKey, rankingSet.size());
    }



    // weekly key update : 1시간 마다 재집계
    @Scheduled(cron = "20 0 * * * *")
    public void refreshWeeklyRanking() {
        aggregateWeekly();
    }

}