package com.tixy.core.util;

import com.tixy.core.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
//Lettuce 를 이용해 Redis Lock 구현 (Redisson 사용 금지!)
//기본구조 Hint - Redis Lock 을 처리하는 별도 LockRedisRepository , LockService 객체를 생성
//다른 비즈니스 로직에서는 LockService 만 의존하는 구조로 개발
public class RedisUtils {
    private final RedisRepository redisRepository;

    public boolean tryLockAll(List<String> keys, long timeout){
        return redisRepository.tryLockAll(keys,timeout);
    }

    public boolean tryLock(String key, String value, long timeoutSeconds) {
        return redisRepository.setIfAbsent(key, value, timeoutSeconds);
    }

    public void unlock(String key, String value) {
        redisRepository.delete(key, value);
    }

    public void unlockAll(List<String> keys) {
        redisRepository.deleteAll(keys);
    }
}
