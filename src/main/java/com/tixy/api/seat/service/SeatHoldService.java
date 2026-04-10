package com.tixy.api.seat.service;

import com.tixy.api.event.service.EventSessionService;
import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.core.exception.seat.SeatErrorCode;
import com.tixy.core.exception.seat.SeatException;
import com.tixy.core.util.RedisUtiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatHoldService {
    private final MemberService memberService;
    private final SeatSessionService seatSessionService;
    private final EventSessionService eventSessionService;
    private final RedisUtiles redisUtiles;

    @Transactional
    public void seatHold(Long eventSessionId, Long seatId, Long memberId) {
        // 유저 검사 안해!
        //지금 판매중인지 검사
        eventSessionService.checkSessionSaleOpen(eventSessionId);
        SeatSession seatSession = seatSessionService.getSeatSession(eventSessionId, seatId);
        // 일단 lock 없이 그냥 점유만...
        boolean isLock = redisUtiles.setSeatHold(eventSessionId.toString() + ":" + seatId.toString() , memberId);
        if(!isLock){
            throw new SeatException(SeatErrorCode.RESERVED_SEAT_SESSION);
        }
        // 좌석 상태 업데이트
        seatSession.setHeld();
    }

    @Transactional
    public void seatHoldNoLock(Long eventSessionId, Long seatId, Long userId) {
        eventSessionService.checkSessionSaleOpen(eventSessionId);

        SeatSession seatSession = seatSessionService.getSeatSession(eventSessionId, seatId);

        redisUtiles.setSeatHold(eventSessionId.toString() + ":" + seatId.toString() , userId);

        seatSession.setHeld();
    }
}
