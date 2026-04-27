package com.tixy.core.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

//    내부적으로 threadId + UUID를 자동 관리
//    isHeldByCurrentThread() 로 소유자 검증
    public boolean tryLockAll(List<String> keys, long timeout) {
        RLock[] locks = keys.stream()
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);

        RLock multiLock = redissonClient.getMultiLock(locks);

        try {
            return multiLock.tryLock(0, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void deleteAll(List<String> keys) {
        RLock[] locks = keys.stream()
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);

        RLock multiLock = redissonClient.getMultiLock(locks);

        if (multiLock.isHeldByCurrentThread()) {
            multiLock.unlock();
        }
    }

    public boolean setIfAbsent(String key, String value, long timeoutSeconds) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofSeconds(timeoutSeconds));
        return Boolean.TRUE.equals(result);
    }

    public void delete(String key, String value) {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
        redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(key),
                value
        );
    }


    // lua스크립트로 여러 좌석 홀딩을 원자적으로 관리.
    // 근데 lua를 쓰면 몇가지 문제점 이있는데
    // 일단 String 이라서 컴파일 에러가 안남.. 거의 메모장 코딩 하는 느낌
    // 그리고 지금 같은 상황에서 단순 setIfAbsent 를 사용하지 않는 이유가 원자성을 보장하기 위해서인데, 그러면 최소 2가지 이상 락을 걸어야 한다는 상황이라는 뜻
    // redisTemplate 에 데이터를 execute 시점에 직렬화 문제가 발생하는데 JdkSerializationRedisSerializer 가 ARGV값을 바이너리로 인코딩해서
    // Lua읽기에 실패한다고 한다.  결국 StringRedisTemplate 를 사용해야 한다. 결국 Stirng 의 제한된 타입으로 저장을 해야 한다. JSON같은거 못넣음..
    // -> 그냥 redisson으로 쓰는게 깔끔함.
//    public boolean tryLockAll(List<String> keys, String value, long timeout) {
//        String script =
//                "local timeout = tonumber(ARGV[2]) " +
//                        "local value = ARGV[1] " +
//                        "for i = 1, #KEYS do " +
//                        "    if redis.call('EXISTS', KEYS[i]) == 1 then " +
//                        "        return 0 " +
//                        "    end " +
//                        "end " +
//                        "for i = 1, #KEYS do " +
//                        "    redis.call('SET', KEYS[i], value, 'EX', timeout) " +
//                        "end " +
//                        "return 1";
//
//        Long result = stringRedisTemplate.execute(
//                new DefaultRedisScript<>(script, Long.class),
//                keys,
//                value, String.valueOf(timeout)
//        );
//
//        if (!Long.valueOf(1).equals(result)) {
//            throw new SeatException(RESERVED_SEAT_SESSION);
//        }
//        return true;
//    }

//    public void deleteAll(List<String> keys, String value) {
//        String script =
//                "for i = 1, #KEYS do " +
//                        "    if redis.call('GET', KEYS[i]) == ARGV[1] then " +
//                        "        redis.call('DEL', KEYS[i]) " +
//                        "    end " +
//                        "end " +
//                        "return 1";
//
//        stringRedisTemplate.execute(
//                new DefaultRedisScript<>(script, Long.class),
//                keys,
//                value
//        );
//    }

}