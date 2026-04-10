package com.tixy.core.util;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.repository.MemberRepository;
import com.tixy.core.util.datainit.EventDataInit;
import com.tixy.core.util.datainit.VenueDataInit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInit implements ApplicationRunner {

    private static final List<MemberSeed> MEMBER_SEEDS = List.of(
            new MemberSeed("Alice", "010-0000-0000"),
            new MemberSeed("Bob", "010-0000-0001")
    );

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // data init
    // venue dummy data 와 event dummy data 를 저장합니다.
    private final VenueDataInit venueDataInit;
    private final EventDataInit eventDataInit;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (memberRepository.count() > 0) return;

        String password = passwordEncoder.encode("abc1234!");
        List<Member> savedMembers = new ArrayList<>();

        for (int i = 0; i < MEMBER_SEEDS.size(); i++) {
            MemberSeed seed = MEMBER_SEEDS.get(i);

            Member member = Member.builder()
                    .email(seed.name() + "@test.com")
                    .password(password)
                    .name(seed.name())
                    .phone(seed.phone())
                    .build();

            Member savedMember = memberRepository.save(member);
            savedMembers.add(savedMember);
        }

        System.out.println("테스트용 계정 2개 세팅 완료!");

        venueDataInit.initVenues(); // 장소 init
        eventDataInit.initEvents(); // event init

    }

    private record MemberSeed(
            String name,
            String phone
    ) {
    }
}