package com.tixy.core.util.datainit;

import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.enums.Location;
import com.tixy.api.venue.enums.VenueStatus;
import com.tixy.api.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VenueDataInit {

    private final VenueRepository venueRepository;

    private static final List<VenueSeed> VENUE_SEEDS = List.of(
            new VenueSeed("올림픽공원 체조경기장", Location.SEOUL),
            new VenueSeed("KSPO DOME", Location.SEOUL),
            new VenueSeed("잠실종합운동장 주경기장", Location.SEOUL),
            new VenueSeed("고척스카이돔", Location.SEOUL),
            new VenueSeed("인스파이어 아레나", Location.GYEONGGI),
            new VenueSeed("수원월드컵경기장", Location.GYEONGGI),
            new VenueSeed("성남아트센터", Location.GYEONGGI),
            new VenueSeed("부산아시아드주경기장", Location.BUSAN),
            new VenueSeed("부산 벡스코", Location.BUSAN),
            new VenueSeed("사직야구장", Location.BUSAN),
            new VenueSeed("강릉아이스아레나", Location.GANGWON),
            new VenueSeed("원주치악체육관", Location.GANGWON),
            new VenueSeed("청주야구장",  Location.CHUNGBUK),
            new VenueSeed("충주체육관", Location.CHUNGBUK),
            new VenueSeed("천안유관순체육관", Location.CHUNGNAM),
            new VenueSeed("아산이순신체육관", Location.CHUNGNAM),
            new VenueSeed("전주월드컵경기장", Location.JEONBUK),
            new VenueSeed("익산실내체육관", Location.JEONBUK),
            new VenueSeed("광주월드컵경기장", Location.JEONNAM),
            new VenueSeed("여수엑스포컨벤션센터", Location.JEONNAM),
            new VenueSeed("포항스틸야드", Location.GYEONGBUK),
            new VenueSeed("안동체육관", Location.GYEONGBUK),
            new VenueSeed("창원종합운동장", Location.GYEONGNAM),
            new VenueSeed("진주실내체육관", Location.GYEONGNAM),
            new VenueSeed("제주월드컵경기장", Location.JEJU),
            new VenueSeed("서귀포칼호텔 야외공연장", Location.JEJU),
            new VenueSeed("청평리조트 야외무대", Location.GANGWON),
            new VenueSeed("구 장충체육관", Location.SEOUL),
            new VenueSeed("대전구장", Location.CHUNGNAM),
            new VenueSeed("동대구역 광장무대", Location.GYEONGBUK)
    );

    @Transactional
    public void initVenues() {
        if (venueRepository.count() > 0) return;

        for (VenueSeed seed : VENUE_SEEDS){
            Venue venue = Venue.builder()
                    .name(seed.name)
                    .venueStatus(VenueStatus.ACTIVE)
                    .location(seed.location)
                    .build();

            venueRepository.save(venue);
        }

        System.out.printf("테스트용 Venue %d개 세팅 완료!",VENUE_SEEDS.size());
    }


    private record VenueSeed(
            String name, Location location
    ) {}
}
