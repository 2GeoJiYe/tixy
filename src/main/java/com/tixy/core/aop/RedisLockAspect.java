package com.tixy.core.aop;

import com.tixy.core.exception.seat.SeatException;
import com.tixy.core.security.annotation.RedisLock;
import com.tixy.core.util.RedisUtiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.tixy.core.exception.seat.SeatErrorCode.RESERVED_SEAT_SESSION;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockAspect {

    private final RedisUtiles redisService;

    @Around("@annotation(redisLock)")
    public Object run(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        // 어노테이션에서 데이터 가져오기
        String keyPreFix = redisLock.key();
        int idx = redisLock.idx();

        // 해당 인덱스에 있는 데이터 가져오기 List<Long> seatIds
        Object[] args = joinPoint.getArgs();
        Object keyIndex = args[idx];

        List<String> keys = null;
        // keyIndex 의 데이터 타입이 리스트 일때
        if (keyIndex instanceof List<?> list) {
            keys = list.stream().map(key -> keyPreFix + key).toList();
            boolean locked = redisService.tryLockAll(keys, redisLock.timeout());

            if (!locked) {
                throw new SeatException(RESERVED_SEAT_SESSION);
            }
        }

        try {
            return joinPoint.proceed();
        } finally {
            redisService.unlockAll(keys);
        }
    }


    // 예약 요청이 들어온 좌석리스트에 대한 예약을 획득하고 하나라도 실패하면 모든 예약을 취소 시킨 다음 에러 반환.
    // 해당 코드의 문제점은 원자성이 보장이 안됨
    // 사람 a 좌석 1,2 요청하고 동시에 사람b가 좌석 2,3 사람 c 좌석 1,2,3을 요청 했을떄
    // 사람 a 좌석 1 획득
    // 사람 c 좌성 2 확득
    // 사람 b 좌석 1,2 실패
    // 사람 a좌성 2 실패
    // c 1를 획득 하려했는데 a의 실패 처리 unlock이 아직 안풀려서 실패
    // -> 결론적으로 싹 다 실패;; 이런 현상이 발생할 수 도 있다  (테스트는 안해봄..)
    // -> 애초에 여러 좌석을 홀딩하는 코드를 원자성있게 lua로 작성해야 할듯?
    private void holdSeat(List<?> list, String keyPreFix, long timeout) {
        // 락 획득 실패 시 관리 하기 위해 키벨류 저장
        Map<String, String> Keys = new HashMap<>();

        try {
            for (Object obj : list) {
                String key = keyPreFix + ":" + obj.toString();
                String value = UUID.randomUUID().toString();

                redisService.tryLock(key, value, timeout);
                // 예약 성공시 키벨류 저장.
                Keys.put(key, value);
            }
        } catch (Exception e) {
            Keys.forEach((key, value) -> {
                try {
                    // 성공한 예약 다시 반환.
                    redisService.unlock(key, value);
                } catch (Exception unlockEx) {
                    log.error("unlock 실패: {}", key, unlockEx);
                }
            });
            throw e;
        }
    }

}
