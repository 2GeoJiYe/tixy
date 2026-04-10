package com.tixy.core.util;

import com.tixy.core.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
//Lettuce 를 이용해 Redis Lock 구현 (Redisson 사용 금지!)
//기본구조 Hint - Redis Lock 을 처리하는 별도 LockRedisRepository , LockService 객체를 생성
//다른 비즈니스 로직에서는 LockService 만 의존하는 구조로 개발
public class RedisUtiles {

    private static final String SEAT_HOLD_PREFIX = "seat-hold:";
    private final RedisRepository redisRepository;



    public boolean setSeatHold(String key, Long userId) {
        return redisRepository.setIfAbsent(SEAT_HOLD_PREFIX + key, userId.toString(), 300);
    }

    public boolean tryLock(String key, String value, long timeoutSeconds) {
        return redisRepository.setIfAbsent(key, value, timeoutSeconds);
    }

    public void unlock(String key, String value) {
        redisRepository.delete(key, value);
    }
}
