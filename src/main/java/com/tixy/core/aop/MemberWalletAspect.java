package com.tixy.core.aop;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.core.annotation.MemberWallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberWalletAspect {

    private final MemberService memberService;

    @Around("@annotation(memberWallet)")
    public Object run(ProceedingJoinPoint joinPoint, MemberWallet memberWallet) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long memberId = (Long) args[memberWallet.idx()];

        Member member = memberService.findById(memberId);
        member.checkMemberWallet();

        return joinPoint.proceed();
    }
}
