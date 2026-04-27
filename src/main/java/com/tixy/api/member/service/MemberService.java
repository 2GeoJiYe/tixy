package com.tixy.api.member.service;

import com.tixy.api.auth.dto.SignUpRequest;
import com.tixy.api.member.entity.Member;
import com.tixy.api.member.repository.MemberRepository;
import com.tixy.core.exception.MemberErrorCode;
import com.tixy.core.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 생성 (auth 도메인에서 회원가입 시 위임받아 실행)
     * 중복 검증 + 비밀번호 암호화 + 저장 처리
     */
    @Transactional
    public Member createMember(SignUpRequest signUpRequest) {
        String email = signUpRequest.email();

        // 이메일/전화번호 중복체크
        if (email != null && memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
                .email(email)
                .point(0L)
                .password(passwordEncoder.encode(signUpRequest.password()))
                .name(signUpRequest.name())
                .build();

        return memberRepository.save(member);
    }

    /**
     * 로그인 ID(이메일) 기반 회원 조회 (auth 도메인에서 로그인 시 위임)
     */
    public Member findByLoginId(String loginId) {
        return memberRepository.findByEmail(loginId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_CREDENTIALS));
    }

    /**
     * ID 기반 회원 조회
     */
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * DB I/O 없이 연관관계 설정용 Proxy 객체만 필요할 때 (post 도메인에서 위임)
     * ex) Post 생성 시 writer FK 설정
     */
    public Member getReferenceById(Long memberId) {
        return memberRepository.getReferenceById(memberId);
    }

    public Optional<Member> findByWalletAddress(String walletAddress) {
        return memberRepository.findByWalletAddress(walletAddress);
    }

    @Transactional
    public void updateWallet(Long memberId, String walletAddress) {
        Member member = findById(memberId);
        member.updateWalletAddress(walletAddress);
    }
}